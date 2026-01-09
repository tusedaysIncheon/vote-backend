package com.vote.votebackend.domain.user.repository;

import com.vote.votebackend.domain.user.entity.UserEntity;
import com.vote.votebackend.domain.user.entity.enums.UserRoleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface UserRepository extends JpaRepository<UserEntity, Long> {




    boolean existsByUsername(String username);

    Optional<UserEntity> findByUsernameAndIsLockAndIsSocial(String username, Boolean isLock, Boolean isSocial);
    Optional<UserEntity> findByUsernameAndIsSocial(String username, Boolean isSocial);
    Optional<UserEntity> findByUsernameAndIsLock(String username, boolean isLock);
    Optional<UserEntity> findByUsername(String username);

    //JWT 발급용 -> 롤타입 확인용
    @Query("SELECT u.roleType FROM UserEntity u WHERE u.username = :username")
    Optional<UserRoleType> findRoleTypeByUsername(String username);

    void deleteByUsername(String username);



}

