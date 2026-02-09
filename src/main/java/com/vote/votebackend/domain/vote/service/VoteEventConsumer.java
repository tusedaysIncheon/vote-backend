package com.vote.votebackend.domain.vote.service;

import com.vote.votebackend.domain.user.entity.UserDetailsEntity;
import com.vote.votebackend.domain.user.entity.UserEntity;
import com.vote.votebackend.domain.user.repository.UserDetailsRepository;
import com.vote.votebackend.domain.user.repository.UserRepository;
import com.vote.votebackend.domain.vote.entity.VoteEntity;
import com.vote.votebackend.domain.vote.entity.VoteOptionEntity;
import com.vote.votebackend.domain.vote.entity.VoteRecordEntity;
import com.vote.votebackend.domain.vote.repository.VoteOptionsRepository;
import com.vote.votebackend.domain.vote.repository.VoteRecordRepository;
import com.vote.votebackend.domain.vote.repository.VoteRepository;
import com.vote.votebackend.global.config.RedisStreamsConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoteEventConsumer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final VoteRepository voteRepository;
    private final VoteRecordRepository voteRecordRepository;
    private final VoteOptionsRepository voteOptionsRepository;
    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;

    // 인스턴스별 고유 이름 생성 (Scale-out 대비)
    private final String consumerName = "vote-consumer-" + UUID.randomUUID().toString();

    @PostConstruct
    public void init() {
        try {
            redisTemplate.opsForStream().createGroup(
                    RedisStreamsConfig.VOTE_STREAM_KEY,
                    RedisStreamsConfig.VOTE_CONSUMER_GROUP);
        } catch (Exception e) {
            log.debug("이미 컨슈머가 생성되어있습니다.");
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void consumeVoteEvents() {
        try {
            List<MapRecord<String, Object, Object>> messages = redisTemplate.opsForStream().read(
                    Consumer.from(RedisStreamsConfig.VOTE_CONSUMER_GROUP, consumerName),
                    StreamReadOptions.empty().count(10).block(Duration.ofMillis(100)),
                    StreamOffset.create(RedisStreamsConfig.VOTE_STREAM_KEY, ReadOffset.lastConsumed()));

            if (messages == null || messages.isEmpty()) {
                return;
            }

            for (MapRecord<String, Object, Object> message : messages) {
                try {
                    processMessage(message);
                } catch (Exception e) {
                    // "Poison Pill" 처리: 처리 불가능한 메시지는 로그만 남기고 ACK 처리하여 건너뜀
                    log.error("Failed to process message ID: {}, Error: {}", message.getId(), e.getMessage());
                }

                // 성공하든 실패하든(catch 후) ACK 전송 -> 다음 메시지 처리를 위해
                redisTemplate.opsForStream().acknowledge(
                        RedisStreamsConfig.VOTE_STREAM_KEY,
                        RedisStreamsConfig.VOTE_CONSUMER_GROUP,
                        message.getId());
            }

        } catch (Exception e) {
            // Redis 연결 문제 등 치명적 오류는 다음 스케줄링으로 미룸
            log.error("Error consuming vote events", e);
        }
    }

    private void processMessage(MapRecord<String, Object, Object> message) {
        Map<Object, Object> body = message.getValue();

        // 데이터 파싱 확인
        if (!body.containsKey("voteId") || !body.containsKey("userId") || !body.containsKey("optionId")) {
            throw new IllegalArgumentException("Invalid message format");
        }

        Long voteId = Long.valueOf((String) body.get("voteId"));
        Long userId = Long.valueOf((String) body.get("userId"));
        Long optionId = Long.valueOf((String) body.get("optionId"));

        log.info("Processing vote event: voteId={}, userId={}, optionId={}", voteId, userId, optionId);

        // 멱등성 체크
        if (voteRecordRepository.existsByVoteIdAndVoterId(voteId, userId)) {
            return;
        }

        VoteEntity vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new IllegalArgumentException("Vote not found: " + voteId));

        UserEntity user = userRepository.getReferenceById(userId);
        UserDetailsEntity details = userDetailsRepository.findByUser(user).orElse(null);

        VoteOptionEntity option = voteOptionsRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("Option not found: " + optionId));

        VoteRecordEntity record = VoteRecordEntity.builder()
                .vote(vote)
                .voter(user)
                .voteOption(option)
                .age(details != null ? details.getAge() : null)
                .mbti(details != null ? details.getMbti() : null)
                .region(details != null ? details.getRegion() : null)
                .gender(details != null ? details.getGender() : null)
                .relationshipStatus(details != null ? details.getRelationshipStatus() : null)
                .build();

        voteRecordRepository.save(record);

        // 카운트 동기화 (트랜잭션이 없으므로 명시적 save 필요)
        vote.increaseTotalVoteCount();
        option.increaseCount();

        voteRepository.save(vote);
        voteOptionsRepository.save(option);
    }
}
