package com.vote.votebackend.domain.user.repository;

import com.vote.votebackend.domain.user.entity.UserDetailsEntity;

import java.util.Optional;


public interface UserDetailsRepositoryCustom {

    Optional<UserDetailsEntity> findProfilebyUserId(Long userId);

}
