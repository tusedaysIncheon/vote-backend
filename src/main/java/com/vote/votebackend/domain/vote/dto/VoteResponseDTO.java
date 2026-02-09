package com.vote.votebackend.domain.vote.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vote.votebackend.domain.user.dto.WriterDTO;
import com.vote.votebackend.domain.user.entity.UserDetailsEntity;
import com.vote.votebackend.domain.user.entity.UserEntity;
import com.vote.votebackend.domain.vote.entity.VoteEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        // 0. 상세 통계 맵 가져오기 (전체 옵션에 대한 데이터)
        Map<Long, Map<String, Map<String, Long>>> detailedStats = stats.getDetailedStats();

        // 1. 옵션별 카운트 및 상세 통계 매핑
        List<VoteOptionResponseDTO> mergedOptions = info.getOptions().stream()
                .map(opt -> {
                    // 해당 옵션의 통계 맵 가져오기 (없으면 빈 맵)
                    Map<String, Map<String, Long>> optionStats = detailedStats.getOrDefault(opt.getId(), new HashMap<>());

                    // [NEW] VoteOptionStatsDTO 빌드
                    VoteOptionStatusDTO statsDTO = VoteOptionStatusDTO.builder()
                            .mbti(optionStats.getOrDefault("mbti", new HashMap<>()))
                            .age(optionStats.getOrDefault("age", new HashMap<>()))
                            .region(optionStats.getOrDefault("region", new HashMap<>()))
                            .status(optionStats.getOrDefault("status", new HashMap<>()))
                            .build();

                    return VoteOptionResponseDTO.builder()
                            .id(opt.getId())
                            .content(opt.getContent())
                            .imageUrl(opt.getImageUrl())
                            .count(stats.getOptionCount().getOrDefault(opt.getId(), 0L))
                            .status(statsDTO) // [NEW] 통계 정보 주입
                            .build();
                })
                .collect(Collectors.toList());

        // 2. 최종 DTO 빌드 (기존과 동일)
        return VoteResponseDTO.builder()
                .id(info.getId())
                .writer(info.getWriter())
                .content(info.getContent())
                .imageUrl(info.getImageUrl())
                .createdAt(LocalDateTime.now())
                .endDate(info.getEndDate())
                .isClosed(LocalDateTime.now().isAfter(info.getEndDate()))
                .totalVote(stats.getTotalVoteCount())
                // .commentCount(0L)
                .options(mergedOptions)
                .build();
    }

    public void setVoteStatus(boolean isVoted, Long votedOptionId) {
        this.isVoted = isVoted;
        this.votedOptionId = votedOptionId;
    }
}
