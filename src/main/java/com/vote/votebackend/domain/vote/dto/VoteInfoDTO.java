package com.vote.votebackend.domain.vote.dto;

import com.vote.votebackend.domain.user.dto.WriterDTO;
import com.vote.votebackend.domain.user.entity.UserDetailsEntity;
import com.vote.votebackend.domain.user.entity.UserEntity;
import com.vote.votebackend.domain.vote.entity.VoteEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteInfoDTO {

    private Long id;
    private WriterDTO writer;
    private String content;
    private String imageUrl;
    private LocalDateTime endDate;
    private List<VoteOptionResponseDTO> options;

    public static VoteInfoDTO toDTO(VoteEntity saveVote) {

        UserEntity writer = saveVote.getWriter();
        UserDetailsEntity userDetails = writer.getUserDetails();

        return VoteInfoDTO.builder()
                .id(saveVote.getId())
                .writer(WriterDTO.builder()
                        .id(writer.getId())
                        .nickname(userDetails != null ? userDetails.getNickname() : "알수없음")
                        .avatarUrl(userDetails != null ? userDetails.getImageUrl() : null)
                        .isFollowing(false)
                        .build())
                .content(saveVote.getContent())
                .imageUrl(saveVote.getImageUrl())
                .endDate(saveVote.getEndDate())
                .options(saveVote.getOptions().stream().map(VoteOptionResponseDTO::toDTO)
                        .collect(Collectors.toList()))
                .build();
    }

}
