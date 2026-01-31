package com.vote.votebackend.domain.vote.service;

import com.vote.votebackend.domain.user.entity.UserEntity;
import com.vote.votebackend.domain.user.repository.UserRepository;
import com.vote.votebackend.domain.vote.dto.VoteOptionRequestDTO;
import com.vote.votebackend.domain.vote.dto.VoteOptionResponseDTO;
import com.vote.votebackend.domain.vote.dto.VoteRequestDTO;
import com.vote.votebackend.domain.vote.dto.VoteResponseDTO;
import com.vote.votebackend.domain.vote.entity.VoteEntity;
import com.vote.votebackend.domain.vote.entity.VoteOptionEntity;
import com.vote.votebackend.domain.vote.entity.VoteRecordEntity;
import com.vote.votebackend.domain.vote.repository.VoteOptionsRepository;
import com.vote.votebackend.domain.vote.repository.VoteRecordRepository;
import com.vote.votebackend.domain.vote.repository.VoteRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final VoteRecordRepository voteRecordRepository;
    private final VoteOptionsRepository voteOptionsRepository;

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

        VoteEntity voteEntity = voteRepository.findById(voteId)
                .orElseThrow(()->new IllegalArgumentException("해당 투표를 찾을 수 없습니다."));

        VoteResponseDTO voteDto = VoteResponseDTO.toDTO(voteEntity);

        if(userId != null){
            voteRecordRepository.findByVoteIdAndVoterId(voteId, userId)
                    .ifPresent(record -> {
                        voteDto.setVoteStatus(true, record.getVoteOption().getId() );
                    });
        }


        return voteDto;
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

}
