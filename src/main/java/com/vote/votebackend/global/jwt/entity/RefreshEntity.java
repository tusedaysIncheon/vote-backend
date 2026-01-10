//package com.vote.votebackend.domain.jwt.entity;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//
//import java.time.LocalDateTime;
//
//@Entity
//@EntityListeners(AuditingEntityListener.class)
//@Table(name = "jwt_refresh_entity")
//@Getter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class RefreshEntity {
//
//    //시퀀스
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//    //사용자아이디
//    @Column(name="username", nullable = false)
//    private String username;
//    //리프레쉬토큰
//    @Column(name="refresh", nullable = false, length = 512)
//    private String refresh;
//    //기기 구분용
//    @Column(name = "device_id")
//    private String deviceId;
//    //생성 시간
//    @CreatedDate
//    @Column(name="created_date", updatable = false)
//    private LocalDateTime createdDate;
//
//
//}
