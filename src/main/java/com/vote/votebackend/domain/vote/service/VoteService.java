package com.vote.votebackend.domain.vote.service;

import com.vote.votebackend.domain.user.entity.UserEntity;
import com.vote.votebackend.domain.user.repository.UserRepository;
import com.vote.votebackend.domain.vote.dto.VoteOptionRequestDTO;
import com.vote.votebackend.domain.vote.dto.VoteOptionResponseDTO;
import com.vote.votebackend.domain.vote.dto.VoteRequestDTO;
import com.vote.votebackend.domain.vote.dto.VoteResponseDTO;
import com.vote.votebackend.domain.vote.entity.VoteEntity;
import com.vote.votebackend.domain.vote.entity.VoteOptionEntity;
import com.vote.votebackend.domain.vote.repository.VoteRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final UserRepository userRepository;

    @Transactional
    public VoteResponseDTO addVote(Long user, @Valid VoteRequestDTO dto) {

        UserEntity writer = userRepository.getReferenceById(user);

        VoteEntity voteEntity = VoteEntity.builder()
                .writer(writer)
                .content(dto.getContent())
                .imageUrl(dto.getImageUrl())
                .endDate(dto.getEndDate())
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
}
