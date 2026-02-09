package com.vote.votebackend.domain.vote.dto;

import com.vote.votebackend.domain.vote.entity.VoteOptionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class VoteOptionResponseDTO {

    private Long id;
    private String content;
    private String imageUrl;
    private Long count;
    private VoteOptionStatusDTO status;

    public static VoteOptionResponseDTO toDTO(VoteOptionEntity voteOptionEntity) {
        return VoteOptionResponseDTO.builder()
                .id(voteOptionEntity.getId())
                .content(voteOptionEntity.getContent())
                .imageUrl(voteOptionEntity.getImageUrl())
                .count(voteOptionEntity.getCount() != null ? voteOptionEntity.getCount() : 0L)
                .build();
    }
}
