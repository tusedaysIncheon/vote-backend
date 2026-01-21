package com.vote.votebackend.domain.user.dto;

public record OAuthLoginResultDTO(
        String accessToken,
        String refreshToken,
        boolean isNewUser,
        boolean needsNickname,
        String username,
        String email,
        String nickname
) {
}
