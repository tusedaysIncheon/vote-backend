package com.vote.votebackend.domain.user.model;

import com.vote.votebackend.domain.user.entity.UserEntity;
import com.vote.votebackend.domain.user.entity.UserDetailsEntity;
import com.vote.votebackend.domain.user.entity.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Year;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDetailsLoadDTO {

    // ==========================================
    // 1. 계정 기본 정보 (UserEntity)
    // ==========================================
    private Long userId;        // 유저 고유 ID (PK)
    private String username;    // 아이디
    private String email;       // 이메일
    private UserRoleType roleType;  // 권한 (USER, ADMIN)
    private Boolean isSocial;   // 소셜 로그인 여부

    // ==========================================
    // 2. 프로필 상세 정보 (UserDetailsEntity)
    // ==========================================
    private String nickname;
    private String imageUrl;    // S3 URL
    private String introduce;

    private Integer birthYear;
    private Integer age;        // (편의성) 출생연도로 계산된 나이
    private Gender gender;
    private MBTI mbti;
    private Region region;
    private RelationshipStatus relationshipStatus;


    private boolean needsProfileSetup; // 프로필 설정이 필요한가? (닉네임 없음)


    // 🔥 [핵심] 생성자: 두 Entity를 받아서 DTO로 변환
    public static UserDetailsLoadDTO from(UserEntity user, UserDetailsEntity details) {
        UserDetailsLoadDTOBuilder builder = UserDetailsLoadDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roleType(user.getRoleType())
                .isSocial(user.getIsSocial());

        // 프로필 정보가 있는 경우 (이미 설정함)
        if (details != null) {
            builder.nickname(details.getNickname())
                    .imageUrl(details.getImageUrl())
                    .introduce(details.getIntroduce())
                    .birthYear(details.getBirthYear())
                    .gender(details.getGender())
                    .mbti(details.getMbti())
                    .region(details.getRegion())
                    .relationshipStatus(details.getRelationshipStatus());

            // 나이 계산
            if (details.getBirthYear() != null) {
                int currentYear = Year.now().getValue();
                builder.age(currentYear - details.getBirthYear() + 1);
            }

            // 닉네임이 있으면 설정 완료된 것으로 간주 -> false
            // 닉네임이 없으면(null) 설정 필요 -> true
            builder.needsProfileSetup(details.getNickname() == null);
        } else {
            // 프로필 정보가 아예 없는 경우 (완전 신규)
            builder.needsProfileSetup(true);
        }

        return builder.build();
    }
}