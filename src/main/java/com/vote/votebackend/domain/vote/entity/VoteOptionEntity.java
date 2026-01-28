package com.vote.votebackend.domain.vote.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vote_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VoteOptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = false)
    private VoteEntity vote;

    @Column(nullable = false)
    private String content; // 항목 텍스트

    @Column(length = 500)
    private String imageUrl; // (선택) 항목 이미지

    // ⚡️ 항목별 득표수 캐싱
    @Builder.Default
    @Column(nullable = false)
    private Long count = 0L;

    // 득표수 증가 메서드
    public void increaseCount() {
        this.count++;
    }
    // 득표수 감소 메서드
    public void decreaseCount() { this.count--; }

    public void setVote(VoteEntity voteEntity) {
        this.vote = voteEntity;
    }
}
