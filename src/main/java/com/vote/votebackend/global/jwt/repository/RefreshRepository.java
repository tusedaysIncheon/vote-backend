//package com.vote.votebackend.domain.jwt.repository;
//
//import com.vote.votebackend.domain.jwt.entity.RefreshEntity;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//public interface RefreshRepository extends JpaRepository<RefreshEntity, Long> {
//
//    Boolean existsByRefresh(String refreshToken);
//
//    @Transactional
//    void deleteByRefresh(String refresh);
//
//    @Transactional
//    void deleteByUsername(String username);
//
//    @Transactional
//    void deleteByUsernameAndDeviceId(String username, String deviceId);
//
//    @Transactional
//    void deleteByCreatedDateBefore(LocalDateTime cutoff);
//
//    @Transactional
//    Optional<RefreshEntity> findByRefresh(String refreshToken);
//
//    @Transactional
//    Optional<RefreshEntity> findByUsernameAndDeviceId(String username, String deviceId);
//
//}
