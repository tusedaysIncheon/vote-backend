package com.vote.votebackend.domain.vote.dto;

import com.vote.votebackend.domain.user.dto.WriterDTO;
import com.vote.votebackend.domain.user.entity.UserDetailsEntity;
import com.vote.votebackend.domain.user.entity.UserEntity;
import com.vote.votebackend.domain.vote.entity.VoteEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VoteResponseDTO {

   private Long id;
   private WriterDTO writer;
   private String content;
   private String imageUrl;
   private LocalDateTime createdAt;
   private LocalDateTime endDate;

   private Long totalVote;
   private Long commentCount;

   private Boolean isVoted;
   private Long votedOptionId;

   private List<VoteOptionResponseDTO> options;


    public static VoteResponseDTO toDTO(VoteEntity saveVote) {

         UserEntity writer = saveVote.getWriter();
         UserDetailsEntity userDetails = writer.getUserDetails();

         return VoteResponseDTO.builder()
                 .id(saveVote.getId())
                 .writer(WriterDTO.builder()
                         .id(writer.getId())
                         .nickname(userDetails != null ? userDetails.getNickname() : "알수없음")
                         .avatarUrl(userDetails != null ? userDetails.getImageUrl() : null)
                         .isFollowing(false)
                         .build())
                 .content(saveVote.getContent())
                 .imageUrl(saveVote.getImageUrl())
                 .createdAt(saveVote.getCreatedDate())
                 .endDate(saveVote.getEndDate())
                 .totalVote(saveVote.getTotalVoteCount())
                 .commentCount(saveVote.getCommentCount())
                 .isVoted(false)
                 .votedOptionId(null)
                 .options(saveVote.getOptions().stream().map(VoteOptionResponseDTO::toDTO)
                         .collect(Collectors.toList()))
                 .build();

    }
}
