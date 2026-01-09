package com.vote.votebackend.domain.user.entity;

import com.vote.votebackend.domain.user.entity.enums.Gender;
import com.vote.votebackend.domain.user.entity.enums.MBTI;
import com.vote.votebackend.domain.user.entity.enums.Region;
import com.vote.votebackend.domain.user.entity.enums.RelationshipStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    // --- 📝 기본 프로필 ---
    @Column(nullable = false, length = 20, unique = true)
    private String nickname;

    @Column(length = 500)
    private String imageUrl; // S3 URL

    @Column(length = 100)
    private String introduce;

    // 1. 나이 (출생년도) - 필수
    @Column(nullable = false)
    private Integer birthYear;

    // 2. 성별 - 필수 (남/여)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    // 3. MBTI - 선택 (NULL 허용)
    @Enumerated(EnumType.STRING)
    @Column(length = 4)
    private MBTI mbti;

    // 4. 거주지 - 필수 (광역 시/도)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Region region;

    // 5. 연애 상태 - (필수)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RelationshipStatus relationshipStatus;


    // Setter 대체
    public void updateProfile(String nickname, String imageUrl, String introduce, MBTI mbti, RelationshipStatus relationshipStatus, Region region, Integer birthYear, Gender gender) {
        if (nickname != null) this.nickname = nickname;
        if (imageUrl != null) this.imageUrl = imageUrl;
        if (introduce != null) this.introduce = introduce;
        if (mbti != null) this.mbti = mbti;
        if (relationshipStatus != null) this.relationshipStatus = relationshipStatus;
        if (region != null) this.region = region;
        if (birthYear != null) this.birthYear = birthYear;
        if (gender != null) this.gender = gender;
    }
}



