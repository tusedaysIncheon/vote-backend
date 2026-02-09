package com.vote.votebackend.domain.user.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;
    private final String username;
    private final com.vote.votebackend.domain.user.entity.UserEntity userEntity;

    public CustomOAuth2User(Map<String, Object> attributes,
            List<GrantedAuthority> authorities,
            String username,
            com.vote.votebackend.domain.user.entity.UserEntity userEntity) {
        this.attributes = attributes;
        this.authorities = authorities;
        this.username = username;
        this.userEntity = userEntity;

    }

    public com.vote.votebackend.domain.user.entity.UserEntity getUserEntity() {
        return userEntity;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return username;
    }
}
