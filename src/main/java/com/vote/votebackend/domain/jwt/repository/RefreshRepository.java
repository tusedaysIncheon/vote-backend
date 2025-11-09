package com.vote.votebackend.domain.jwt.repository;
import java.util.Optional;
import com.vote.votebackend.domain.jwt.entity.RefreshEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface RefreshRepository extends JpaRepository<RefreshEntity, Long> {

    Boolean existsByRefresh(String refreshToken);

    @Transactional
    void deleteByRefresh(String refresh);

    @Transactional
    void deleteByUsername(String username);

    @Transactional
    void deleteByUsernameAndDeviceId(String username, String deviceId);

    @Transactional
    void deleteByCreatedDateBefore(LocalDateTime cutoff);

    @Transactional
    Optional<RefreshEntity> findByRefresh(String refreshToken);
}
