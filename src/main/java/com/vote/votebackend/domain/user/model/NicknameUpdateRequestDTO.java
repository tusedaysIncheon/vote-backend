package com.vote.votebackend.domain.user.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record NicknameUpdateRequestDTO(
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 작성해주세요.")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9._-]+$", message = "닉네임은 한글/영문/숫자/._-만 허용합니다.")
        String nickname
) {}