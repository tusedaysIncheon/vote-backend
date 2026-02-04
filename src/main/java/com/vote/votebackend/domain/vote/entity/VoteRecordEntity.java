package com.vote.votebackend.domain.vote.entity;

import com.vote.votebackend.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "vote_records", uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_vote", // 한 유저는 한 투표에 한 번만!
                                columnNames = { "user_id", "vote_id" })
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class VoteRecordEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        // 투표한 사람
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private UserEntity voter;

        // 어떤 투표인지
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "vote_id", nullable = false)
        private VoteEntity vote;

        // 무엇을 선택했는지 (통계용)
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "vote_option_id", nullable = false)
        private VoteOptionEntity voteOption;

        @CreatedDate
        private LocalDateTime createdDate;

        // --- 📸 통계용 스냅샷 (투표 당시의 유저 상태 박제) ---
        @Enumerated(EnumType.STRING)
        @Column(length = 4)
        private com.vote.votebackend.domain.user.entity.enums.MBTI mbti;

        @Enumerated(EnumType.STRING)
        @Column(length = 20)
        private com.vote.votebackend.domain.user.entity.enums.Region region;

        private Integer age; // 투표 당시 나이

        @Enumerated(EnumType.STRING)
        @Column(length = 10)
        private com.vote.votebackend.domain.user.entity.enums.Gender gender;

        @Enumerated(EnumType.STRING)
        @Column(length = 20)
        private com.vote.votebackend.domain.user.entity.enums.RelationshipStatus relationshipStatus;

}
