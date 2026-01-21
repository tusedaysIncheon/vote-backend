package com.vote.votebackend.domain.user.dto;

public record AuthLoginResponseDTO(
        String accessToken,
        UserResponseDTO user
) {
}
