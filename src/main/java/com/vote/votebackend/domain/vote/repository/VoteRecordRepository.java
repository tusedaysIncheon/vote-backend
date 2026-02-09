package com.vote.votebackend.domain.vote.repository;

import com.vote.votebackend.domain.vote.entity.VoteRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoteRecordRepository extends JpaRepository<VoteRecordEntity, Long> {

    boolean existsByVoteIdAndVoterId(Long voteId, Long userId);

    Optional<VoteRecordEntity> findByVoteIdAndVoterId(Long voteId, Long voterId);
}
