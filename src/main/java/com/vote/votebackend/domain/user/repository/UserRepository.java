package com.vote.votebackend.domain.user.repository;

import com.vote.votebackend.domain.user.entity.UserEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByUsername(String username);

    Optional<UserEntity> findByUsernameAndIsLockAndIsSocial(String username, Boolean isLock, Boolean isSocial);
    Optional<UserEntity> findByUsernameAndIsSocial(String username, Boolean isSocial);
}

