package com.vote.votebackend.domain.user.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NicknameUpdateRequestDTO(
        @NotBlank
        @Size(min = 2, max = 10)
        String nickname
) {}
