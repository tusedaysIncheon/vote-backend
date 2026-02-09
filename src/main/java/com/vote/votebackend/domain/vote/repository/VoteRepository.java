package com.vote.votebackend.domain.vote.repository;

import com.vote.votebackend.domain.vote.entity.VoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<VoteEntity, Long>, VoteRepositoryCustom {

    @EntityGraph(attributePaths = { "writer", "writer.userDetails" })
    Optional<VoteEntity> findById(Long id);

}
