package com.vote.votebackend.domain.vote.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VoteInteractDTO {

    @NotNull(message = "투표 항목을 선택해주세요.")
    private Long optionId;

}
