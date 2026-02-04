package com.vote.votebackend.domain.vote.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vote.votebackend.domain.user.entity.UserEntity;
import com.vote.votebackend.domain.user.repository.UserRepository;
import com.vote.votebackend.domain.vote.dto.*;
import com.vote.votebackend.domain.vote.entity.VoteEntity;
import com.vote.votebackend.domain.vote.entity.VoteOptionEntity;
import com.vote.votebackend.domain.vote.entity.VoteRecordEntity;
import com.vote.votebackend.domain.vote.repository.VoteOptionsRepository;
import com.vote.votebackend.domain.vote.repository.VoteRecordRepository;
import com.vote.votebackend.domain.vote.repository.VoteRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final VoteRecordRepository voteRecordRepository;
    private final VoteOptionsRepository voteOptionsRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public VoteResponseDTO createVote(Long user, @Valid VoteRequestDTO dto) {

        UserEntity writer = userRepository.getReferenceById(user);

        LocalDateTime endDate = LocalDateTime.now().plusHours(dto.getDuration());

        VoteEntity voteEntity = VoteEntity.builder()
                .writer(writer)
                .content(dto.getContent())
                .imageUrl(dto.getImageUrl())
                .endDate(endDate)
                .build();

        for(VoteOptionRequestDTO optionsDTO: dto.getOptions()){
            VoteOptionEntity option = VoteOptionEntity.builder()
                    .content(optionsDTO.getContent())
                    .imageUrl(optionsDTO.getImageUrl())
                    .build();

            voteEntity.addOption(option);
        }

        VoteEntity saveVote = voteRepository.save(voteEntity);

        return VoteResponseDTO.toDTO(saveVote);
    }

    @Transactional(readOnly = true)
    public Page<VoteResponseDTO> getFeedList(Pageable pageable) {
        Page<VoteEntity> votePage = voteRepository.findRecommendedVotes(pageable);

        return votePage.map(VoteResponseDTO::toDTO);
    }

    @Transactional(readOnly = true)
    public VoteResponseDTO getVoteDetails(Long voteId, Long userId) {

        VoteInfoDTO info = getVoteInfoWithType(voteId,userId);
        VoteStatsDTO stats = getVoteStatus(voteId);

        VoteResponseDTO response = VoteResponseDTO.merge(info, stats);

        if(userId != null){
            voteRecordRepository.findByVoteIdAndVoterId(voteId, userId)
                    .ifPresent(record -> {
                        response.setVoteStatus(true, record.getVoteOption().getId() );
                    });
        }


        return response;
    }


    @Transactional
    public void castVote(Long voteId, Long userId, Long optionId) {
        VoteEntity vote = voteRepository.findById(voteId)
                .orElseThrow(()->new IllegalArgumentException("해당 투표를 찾을 수 없습니다."));

        if(vote.isClosed()) {
            throw new IllegalArgumentException("이미 마감된 투표입니다.");
        }

        if(voteRecordRepository.existsByVoteIdAndVoterId(voteId, userId)){
            throw new IllegalArgumentException("이미 참여한 투표입니다.");
        }

        VoteOptionEntity option = voteOptionsRepository.findById(optionId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 옵션입니다."));

        UserEntity user = userRepository.getReferenceById(userId);

        VoteRecordEntity record = VoteRecordEntity.builder()
                .vote(vote)
                .voter(user)
                .voteOption(option)
                .build();

        voteRecordRepository.save(record);

        vote.increaseTotalVoteCount();
        option.increaseCount();
    }


    private VoteInfoDTO getVoteInfoWithType(Long voteId, Long userId) {

        String cacheKey = "vote:info:"+voteId;

        String jsonStr = (String)redisTemplate.opsForValue().get(cacheKey);

        if(jsonStr != null){
            log.info("🔥 Cache HIT! (Redis에서 가져옴)");
            try{
                return
                        objectMapper.readValue(jsonStr,VoteInfoDTO.class);
            } catch (Exception e){

                log.error("레디스 파싱 에러", e);
            }

        }

        log.info("🐢 Cache MISS.. (DB에서 조회)");

        VoteEntity vote = voteRepository.findById(voteId)
                .orElseThrow(()-> new IllegalArgumentException("투표를 찾을 수 없습니다."));

        VoteInfoDTO info = VoteInfoDTO.toDTO(vote);
        try{
            String jsonValue = objectMapper.writeValueAsString(info);
            redisTemplate.opsForValue().set(cacheKey,jsonValue, Duration.ofDays(1));
        } catch (Exception e){
            log.error("레디스 시얼라이즈 에러", e);
        }

        return info;
    }

    private VoteStatsDTO getVoteStatus(Long voteId) {
        String cacheKey = "vote:status:"+voteId;
        Map<Object, Object> rawMap = redisTemplate.opsForHash().entries(cacheKey);

        return VoteStatsDTO.toRedisMap(voteId, rawMap);
    }

}
