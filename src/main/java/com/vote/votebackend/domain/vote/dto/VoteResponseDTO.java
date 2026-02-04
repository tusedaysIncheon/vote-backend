package com.vote.votebackend.domain.vote.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
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
   private boolean isClosed;

   private List<VoteOptionResponseDTO> options;


    public static VoteResponseDTO toDTO(VoteEntity saveVote) {

         UserEntity writer = saveVote.getWriter();
         UserDetailsEntity userDetails = writer.getUserDetails();

         boolean isClosedNow = LocalDateTime.now().isAfter(saveVote.getEndDate());

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
                 .isClosed(isClosedNow)
                 .isVoted(false)
                 .votedOptionId(null)
                 .options(saveVote.getOptions().stream().map(VoteOptionResponseDTO::toDTO)
                         .collect(Collectors.toList()))
                 .build();

    }

    public static VoteResponseDTO merge(VoteInfoDTO info, VoteStatsDTO stats) {

        // 1. 옵션별 카운트 매핑 (Stats에서 가져와서 Info에 덮어쓰기)
        List<VoteOptionResponseDTO> mergedOptions = info.getOptions().stream()
                .map(opt -> VoteOptionResponseDTO.builder()
                        .id(opt.getId())
                        .content(opt.getContent())
                        .imageUrl(opt.getImageUrl())
                        .count(stats.getOptionCount().getOrDefault(opt.getId(), 0L)) // ⭐ 여기가 핵심! Redis 카운트 사용
                        .build())
                .collect(Collectors.toList());
        // 2. 최종 DTO 빌드
        return VoteResponseDTO.builder()
                .id(info.getId())
                .writer(info.getWriter())
                .content(info.getContent())
                .imageUrl(info.getImageUrl())
                .createdAt(LocalDateTime.now()) // 혹은 Info에 추가
                .endDate(info.getEndDate())
                .isClosed(LocalDateTime.now().isAfter(info.getEndDate()))

                // 통계 데이터 주입
                .totalVote(stats.getTotalVoteCount())
                // .commentCount(0L) // 댓글 수는 별도 조회 필요하면 처리
                .options(mergedOptions)
                .build();
    }

    public void setVoteStatus(boolean isVoted, Long votedOptionId) {
        this.isVoted = isVoted;
        this.votedOptionId = votedOptionId;
    }
}
