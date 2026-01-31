package com.vote.votebackend.domain.vote.repository;

import com.vote.votebackend.domain.vote.entity.VoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface VoteRepository extends JpaRepository<VoteEntity,Long>, VoteRepositoryCustom {


}
