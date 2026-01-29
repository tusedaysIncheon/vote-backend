package com.vote.votebackend.domain.vote.repository;

import com.vote.votebackend.domain.vote.entity.VoteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VoteRepositoryCustom {
    Page<VoteEntity> findRecommendedVotes(Pageable pageable);
}
