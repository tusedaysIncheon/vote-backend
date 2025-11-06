package com.vote.votebackend.domain.user.repository;

import ch.qos.logback.core.testUtil.MockInitialContext;
import com.vote.votebackend.domain.user.entity.UserEntity;
import com.vote.votebackend.domain.user.entity.UserRoleType;
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

    //JWT 발급용 -> 롤타입 확인용
    @Query("SELECT u.roleType FROM UserEntity u WHERE u.username = :username")
    Optional<UserRoleType> findRoleTypeByUsername(String username);

    void deleteByUsername(String username);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update UserEntity u
    set u.nickname = :nickname, u.needsNickname = false
    where u.username = :username
    """)
    void updateNicknameByUsername(
            @Param("username") String username,
            @Param("nickname")
            @NotBlank
            @Size(min = 2, max = 10)
            @Pattern(regexp = "^[가-힣a-zA-Z0-9._-]+$", message = "닉네임은 한글/영문/숫자/._-만 허용합니다.")
            String nickname
    );


}

