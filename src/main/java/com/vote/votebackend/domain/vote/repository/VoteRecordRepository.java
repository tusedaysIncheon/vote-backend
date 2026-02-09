package com.vote.votebackend.domain.vote.repository;

import com.vote.votebackend.domain.vote.entity.VoteRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoteRecordRepository extends JpaRepository<VoteRecordEntity, Long> {

    boolean existsByVoteIdAndVoterId(Long voteId, Long userId);

    Optional<VoteRecordEntity> findByVoteIdAndVoterId(Long voteId, Long voterId);

    List<VoteRecordEntity> findByVoterIdAndVoteIdIn(Long voterId, List<Long> voteIds);
}
