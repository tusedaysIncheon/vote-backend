package com.vote.votebackend.domain.vote.entity;


import com.vote.votebackend.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "votes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class VoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자 (UserEntity와 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity writer;

    @Column(nullable = false, length = 300)
    private String content; // 질문 내용

    @Column(length = 500)
    private String imageUrl; // (선택) 투표 관련 이미지

    @Column(nullable = false)
    private LocalDateTime endDate; // 마감 시간

    // --- ⚡️ 성능 최적화용 카운트 (반정규화) ---
    @Builder.Default
    @Column(nullable = false)
    private Long totalVoteCount = 0L;

    @Builder.Default
    @Column(nullable = false)
    private Long commentCount = 0L;

    // 투표 항목들 (양방향 매핑)
    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VoteOptionEntity> options = new ArrayList<>();

    // 시간 정보
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    // 비즈니스 로직: 카운트 증가
    public void increaseCommentCount() {
        this.commentCount++;
    }
    public void increaseTotalVoteCount() {
        this.totalVoteCount++;
    }

    // 비즈니스 로직 : 카운트 감소
    public void decreaseCommentCount(){this.commentCount--;}
    public void decreaseTotalVoteCount(){this.totalVoteCount--;}

    //투표 옵션 Id 부여
    public void addOption(VoteOptionEntity option) {
        this.options.add(option);
        option.setVote(this);
    }
}