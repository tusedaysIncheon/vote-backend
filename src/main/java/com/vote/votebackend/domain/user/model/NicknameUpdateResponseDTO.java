package com.vote.votebackend.domain.user.model;

public record NicknameUpdateResponseDTO(
        String nickname,
        boolean needsNickname
) {
}
