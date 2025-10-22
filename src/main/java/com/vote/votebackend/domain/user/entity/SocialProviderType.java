package com.vote.votebackend.domain.user.entity;

public enum SocialProviderType {

    NAVER("네이버"),
    GOOGLE("구글");

    private final String description;
    SocialProviderType(String description) {
        this.description = description;
    }

}
