package com.vote.votebackend.domain.vote.repository;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vote.votebackend.domain.user.entity.QUserEntity;
import com.vote.votebackend.domain.vote.entity.VoteEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static com.vote.votebackend.domain.user.entity.QUserEntity.userEntity;
import static com.vote.votebackend.domain.vote.entity.QVoteEntity.voteEntity;

@RequiredArgsConstructor
public class VoteRepositoryCustomImpl implements  VoteRepositoryCustom {
    private final JPAQueryFactory  queryFactory;


    @Override
    public Page<VoteEntity> findRecommendedVotes(Pageable pageable) {

        LocalDateTime now = LocalDateTime.now();

        //gravity 알고리즘 수식 정의
        // totalVote * 2 + commentCount *5 + 10 / POWER(시간차 + 2, 1.5)

        //분자->반응점수
        NumberExpression<Long> reactionScore = voteEntity.totalVoteCount.multiply(2)
                .add(voteEntity.commentCount.multiply(5))
                .add(10);

        //분모->시간 감쇠
        NumberExpression<Double> timeDecay = Expressions.numberTemplate(Double.class,
                "power(( (extract(epoch from cast({1} as timestamp)) - extract(epoch from {0})) / 3600.0) + 2, 1.5)",
                voteEntity.createdDate, now);

        NumberExpression<Double> gravityScore = reactionScore.castToNum(Double.class).divide(timeDecay);

        List<VoteEntity> content = queryFactory
                .selectFrom(voteEntity)
                .leftJoin(voteEntity.writer, userEntity).fetchJoin()
                .where(voteEntity.endDate.after(now))
                .orderBy(gravityScore.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(voteEntity.count())
                .from(voteEntity)
                .where(voteEntity.endDate.after(now))
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }
}
