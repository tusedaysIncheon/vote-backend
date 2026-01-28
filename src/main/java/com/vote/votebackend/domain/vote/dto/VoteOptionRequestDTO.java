package com.vote.votebackend.domain.vote.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VoteOptionRequestDTO {

    @NotBlank(message = "항목의 이름은 반드시 필요합니다.")
    private String content;
    private String imageUrl;

}
