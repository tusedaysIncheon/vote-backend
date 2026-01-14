package com.vote.votebackend.domain.user.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vote.votebackend.domain.user.entity.UserDetailsEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.vote.votebackend.domain.user.entity.QUserDetailsEntity.userDetailsEntity;
import static com.vote.votebackend.domain.user.entity.QUserEntity.userEntity;

@Repository
@RequiredArgsConstructor
public class UserDetailsRepositoryCustomImpl implements UserDetailsRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<UserDetailsEntity> findProfilebyUserId(Long userId) { // 타입 Long으로 변경
        return Optional.ofNullable(
                queryFactory.selectFrom(userDetailsEntity)
                        .join(userDetailsEntity.user, userEntity).fetchJoin()
                        .where(userEntity.id.eq(userId)) // 이제 타입이 맞음 (Long == Long)
                        .fetchOne()
        );
    }
}


