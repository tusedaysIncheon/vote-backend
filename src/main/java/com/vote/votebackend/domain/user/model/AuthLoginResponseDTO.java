package com.vote.votebackend.domain.user.model;

public record AuthLoginResponseDTO(
        String accessToken,
        UserResponseDTO user
) {
}
