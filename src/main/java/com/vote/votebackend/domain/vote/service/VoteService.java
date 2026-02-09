package com.vote.votebackend.domain.vote.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vote.votebackend.domain.user.entity.UserDetailsEntity;
import com.vote.votebackend.domain.user.entity.UserEntity;
import com.vote.votebackend.domain.user.repository.UserRepository;
import com.vote.votebackend.domain.vote.dto.*;
import com.vote.votebackend.domain.vote.entity.VoteEntity;
import com.vote.votebackend.domain.vote.entity.VoteOptionEntity;
import com.vote.votebackend.domain.vote.entity.VoteRecordEntity;
import com.vote.votebackend.domain.vote.repository.VoteOptionsRepository;
import com.vote.votebackend.domain.vote.repository.VoteRecordRepository;
import com.vote.votebackend.domain.vote.repository.VoteRepository;
import com.vote.votebackend.global.config.RedisStreamsConfig;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

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

        for (VoteOptionRequestDTO optionsDTO : dto.getOptions()) {
            VoteOptionEntity option = VoteOptionEntity.builder()
                    .content(optionsDTO.getContent())
                    .imageUrl(optionsDTO.getImageUrl())
                    .build();

            voteEntity.addOption(option);
        }

        VoteEntity saveVote = voteRepository.save(voteEntity);

        return VoteResponseDTO.toDTO(saveVote);
    }

    // redis MGET을 활용한 피드 목록 조회
    @Transactional(readOnly = true)
    public Page<VoteResponseDTO> getFeedList(Pageable pageable) {
        Page<VoteEntity> votePage = voteRepository.findRecommendedVotes(pageable);
        List<Long> voteIds = votePage.stream().map(VoteEntity::getId).toList();

        if (voteIds.isEmpty()) {
            return Page.empty(pageable);
        }

        List<String> keys = voteIds.stream().map(id -> "vote:info:" + id).toList();
        List<Object> cachedValues = redisTemplate.opsForValue().multiGet(keys);
        List<VoteResponseDTO> dtos = new ArrayList<>();

        for (int i = 0; i < voteIds.size(); i++) {
            Long voteId = voteIds.get(i);
            String jsonStr = (cachedValues != null && cachedValues.size() > i) ? (String) cachedValues.get(i) : null;

            VoteInfoDTO info = null; // ⭐ VoteResponseDTO -> VoteInfoDTO 로 변경

            if (jsonStr != null) {
                try {
                    // ⭐ VoteInfoDTO로 역직렬화
                    info = objectMapper.readValue(jsonStr, VoteInfoDTO.class);
                } catch (Exception e) {
                    log.error("레디스 파싱에러", e);
                }
            }

            if (info == null) {
                VoteEntity entity = votePage.getContent().get(i);
                info = VoteInfoDTO.toDTO(entity); // ⭐ 엔티티 -> VoteInfoDTO 변환
                try {
                    redisTemplate.opsForValue().set("vote:info:" + voteId, objectMapper.writeValueAsString(info),
                            Duration.ofDays(1));
                } catch (Exception e) {
                    log.error("레디스 시리얼라이즈 에러", e);
                }
            }

            VoteStatsDTO stats = getVoteStatus(voteId);
            VoteResponseDTO dto = VoteResponseDTO.merge(info, stats);

            dtos.add(dto);
        }

        return new PageImpl<>(dtos, pageable, votePage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public VoteResponseDTO getVoteDetails(Long voteId, Long userId) {

        VoteInfoDTO info = getVoteInfoWithType(voteId, userId);
        VoteStatsDTO stats = getVoteStatus(voteId);

        VoteResponseDTO response = VoteResponseDTO.merge(info, stats);

        if (userId != null) {
            voteRecordRepository.findByVoteIdAndVoterId(voteId, userId)
                    .ifPresent(record -> {
                        response.setVoteStatus(true, record.getVoteOption().getId());
                    });
        }

        return response;
    }

    @Transactional
    public void castVote(Long voteId, Long userId, Long optionId) {
        VoteEntity vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new IllegalArgumentException("해당 투표를 찾을 수 없습니다."));

        if (vote.isClosed()) {
            throw new IllegalArgumentException("이미 마감된 투표입니다.");
        }

        String voterKey = "vote:voters:" + voteId;
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(voterKey, userId.toString()))) {
            throw new IllegalArgumentException("이미 참여한 투표입니다.");
        }

        if (voteRecordRepository.existsByVoteIdAndVoterId(voteId, userId)) {

            redisTemplate.opsForSet().add(voterKey, userId.toString());
            throw new IllegalArgumentException("이미 참여한 투표입니다.");
        }

        UserEntity user = userRepository.getReferenceById(userId);
        UserDetailsEntity details = user.getUserDetails();

        String statsKey = "vote:stats:" + voteId;
        redisTemplate.opsForHash().increment(statsKey, "total", 1);
        redisTemplate.opsForHash().increment(statsKey, "opt:" + optionId, 1);

        if (details != null) {
            incrementStat(statsKey, optionId, "mbti", details.getMbti());
            incrementStat(statsKey, optionId, "age", details.getAgeGroup());
            incrementStat(statsKey, optionId, "region", details.getRegion());
            incrementStat(statsKey, optionId, "status", details.getRelationshipStatus());
        }

        redisTemplate.opsForSet().add(voterKey, userId.toString());

        Map<String, String> streamData = new HashMap<>();
        streamData.put("voteId", voteId.toString());
        streamData.put("userId", userId.toString());
        streamData.put("optionId", optionId.toString());

        redisTemplate.opsForStream().add(RedisStreamsConfig.VOTE_STREAM_KEY, streamData);

    }

    private VoteInfoDTO getVoteInfoWithType(Long voteId, Long userId) {

        String cacheKey = "vote:info:" + voteId;

        String jsonStr = (String) redisTemplate.opsForValue().get(cacheKey);

        if (jsonStr != null) {
            log.info("🔥 Cache HIT! (Redis에서 가져옴)");
            try {
                return objectMapper.readValue(jsonStr, VoteInfoDTO.class);
            } catch (Exception e) {

                log.error("레디스 파싱 에러", e);
            }

        }

        log.info("🐢 Cache MISS.. (DB에서 조회)");

        VoteEntity vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new IllegalArgumentException("투표를 찾을 수 없습니다."));

        VoteInfoDTO info = VoteInfoDTO.toDTO(vote);
        try {
            String jsonValue = objectMapper.writeValueAsString(info);
            redisTemplate.opsForValue().set(cacheKey, jsonValue, Duration.ofDays(1));
        } catch (Exception e) {
            log.error("레디스 시얼라이즈 에러", e);
        }

        return info;
    }

    private VoteStatsDTO getVoteStatus(Long voteId) {
        String cacheKey = "vote:status:" + voteId;
        Map<Object, Object> rawMap = redisTemplate.opsForHash().entries(cacheKey);

        return VoteStatsDTO.toRedisMap(voteId, rawMap);
    }

    private void incrementStat(String key, Long optionId, String category, Object value) {
        if (value != null) {
            String field = "opt:" + optionId + ":" + category + ":" + value.toString();
            redisTemplate.opsForHash().increment(key, field, 1);
        }
    }

}
