package com.vote.votebackend.domain.vote.repository;

import com.vote.votebackend.domain.vote.entity.VoteOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteOptionsRepository extends JpaRepository<VoteOptionEntity,Long> {



}
