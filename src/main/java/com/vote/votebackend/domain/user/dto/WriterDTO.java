package com.vote.votebackend.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class WriterDTO {

    private Long id;
    private String nickname;
    private String avatarUrl;
    private Boolean isFollowing;

}
