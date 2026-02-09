package com.vote.votebackend.domain.vote.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vote.votebackend.domain.user.entity.UserDetailsEntity;
import com.vote.votebackend.domain.user.entity.UserEntity;
import com.vote.votebackend.domain.user.repository.UserRepository;
import com.vote.votebackend.domain.vote.dto.*;
import com.vote.votebackend.domain.vote.entity.VoteEntity;
import com.vote.votebackend.domain.vote.entity.VoteOptionEntity;
import com.vote.votebackend.domain.vote.entity.VoteRecordEntity;
import com.vote.votebackend.domain.vote.repository.VoteOptionsRepository;
import com.vote.votebackend.domain.vote.repository.VoteRecordRepository;
import com.vote.votebackend.domain.vote.repository.VoteRepository;
import com.vote.votebackend.global.config.RedisStreamsConfig;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final VoteRecordRepository voteRecordRepository;
    private final VoteOptionsRepository voteOptionsRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public VoteResponseDTO createVote(Long user, @Valid VoteRequestDTO dto) {

        UserEntity writer = userRepository.getReferenceById(user);

        LocalDateTime endDate = LocalDateTime.now().plusHours(dto.getDuration());

        VoteEntity voteEntity = VoteEntity.builder()
                .writer(writer)
                .content(dto.getContent())
                .imageUrl(dto.getImageUrl())
                .endDate(endDate)
                .build();

        for (VoteOptionRequestDTO optionsDTO : dto.getOptions()) {
            VoteOptionEntity option = VoteOptionEntity.builder()
                    .content(optionsDTO.getContent())
                    .imageUrl(optionsDTO.getImageUrl())
                    .build();

            voteEntity.addOption(option);
        }

        VoteEntity saveVote = voteRepository.save(voteEntity);

        return VoteResponseDTO.toDTO(saveVote);
    }

    // redis MGET을 활용한 피드 목록 조회
    @Transactional(readOnly = true)
    public Page<VoteResponseDTO> getFeedList(Pageable pageable, Long userId) {
        Page<VoteEntity> votePage = voteRepository.findRecommendedVotes(pageable);
        List<Long> voteIds = votePage.stream().map(VoteEntity::getId).toList();

        if (voteIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // 로그인한 유저라면, 현재 페이지의 투표들에 대해 투표 여부 조회 (Batch Query)
        Map<Long, Long> userVoteMap = new HashMap<>();
        if (userId != null) {
            List<VoteRecordEntity> records = voteRecordRepository.findByVoterIdAndVoteIdIn(userId, voteIds);
            log.info("📢 피드 조회 - 사용자: {}, 조회된 투표 수: {}, 참여한 투표 수: {}", userId, voteIds.size(), records.size());
            for (VoteRecordEntity record : records) {
                userVoteMap.put(record.getVote().getId(), record.getVoteOption().getId());
            }
        } else {
            log.info("📢 피드 조회 - 비로그인 사용자");
        }

        List<String> keys = voteIds.stream().map(id -> "vote:info:" + id).toList();
        List<Object> cachedValues = redisTemplate.opsForValue().multiGet(keys);
        List<VoteResponseDTO> dtos = new ArrayList<>();

        for (int i = 0; i < voteIds.size(); i++) {
            Long voteId = voteIds.get(i);
            String jsonStr = (cachedValues != null && cachedValues.size() > i) ? (String) cachedValues.get(i) : null;

            VoteInfoDTO info = null; // ⭐ VoteResponseDTO -> VoteInfoDTO 로 변경

            if (jsonStr != null) {
                try {
                    // ⭐ VoteInfoDTO로 역직렬화
                    info = objectMapper.readValue(jsonStr, VoteInfoDTO.class);
                } catch (Exception e) {
                    log.error("레디스 파싱에러", e);
                }
            }

            if (info == null) {
                VoteEntity entity = votePage.getContent().get(i);
                info = VoteInfoDTO.toDTO(entity); // ⭐ 엔티티 -> VoteInfoDTO 변환

                try {
                    redisTemplate.opsForValue().set("vote:info:" + voteId, objectMapper.writeValueAsString(info),
                            Duration.ofDays(1));
                } catch (Exception e) {
                    log.error("레디스 시리얼라이즈 에러", e);
                }
            }

            VoteEntity entity = votePage.getContent().get(i);
            VoteStatsDTO stats = getVoteStatus(entity);
            VoteResponseDTO dto = VoteResponseDTO.merge(info, stats);

            // 사용자가 투표했다면 해당 정보 설정
            if (userVoteMap.containsKey(voteId)) {
                dto.setVoteStatus(true, userVoteMap.get(voteId));
            }

            dtos.add(dto);
        }

        return new PageImpl<>(dtos, pageable, votePage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public VoteResponseDTO getVoteDetails(Long voteId, Long userId) {

        VoteInfoDTO info = getVoteInfoWithType(voteId, userId);

        // VoteEntity 조회 (getVoteInfoWithType에서 캐시 미스 시 조회하긴 하지만, 여기서 다시 조회 필요할 수 있음)
        // 최적화를 위해 getVoteInfoWithType이 Entity를 반환하도록 하거나, 여기서 조회
        VoteEntity vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new IllegalArgumentException("투표를 찾을 수 없습니다."));

        VoteStatsDTO stats = getVoteStatus(vote);

        VoteResponseDTO response = VoteResponseDTO.merge(info, stats);

        if (userId != null) {
            voteRecordRepository.findByVoteIdAndVoterId(voteId, userId)
                    .ifPresent(record -> {
                        response.setVoteStatus(true, record.getVoteOption().getId());
                    });
        }

        return response;
    }

    // ... (castVote method remains unchanged) ...

    @Transactional
    public void castVote(Long voteId, Long userId, Long optionId) {
        VoteEntity vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new IllegalArgumentException("해당 투표를 찾을 수 없습니다."));

        if (vote.isClosed()) {
            throw new IllegalArgumentException("이미 마감된 투표입니다.");
        }

        String voterKey = "vote:voters:" + voteId;
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(voterKey, userId.toString()))) {
            throw new IllegalArgumentException("이미 참여한 투표입니다.");
        }

        if (voteRecordRepository.existsByVoteIdAndVoterId(voteId, userId)) {

            redisTemplate.opsForSet().add(voterKey, userId.toString());
            throw new IllegalArgumentException("이미 참여한 투표입니다.");
        }

        UserEntity user = userRepository.getReferenceById(userId);
        UserDetailsEntity details = user.getUserDetails();

        String statsKey = "vote:stats:" + voteId;

        // Redis에 키가 없으면 DB에서 복구 후 증가
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(statsKey))) {
            restoreVoteStats(vote);
        }

        redisTemplate.opsForHash().increment(statsKey, "total", 1);
        redisTemplate.opsForHash().increment(statsKey, "opt:" + optionId, 1);

        if (details != null) {
            incrementStat(statsKey, optionId, "mbti", details.getMbti());
            incrementStat(statsKey, optionId, "age", details.getAgeGroup());
            incrementStat(statsKey, optionId, "region", details.getRegion());
            incrementStat(statsKey, optionId, "status", details.getRelationshipStatus());
        }

        redisTemplate.opsForSet().add(voterKey, userId.toString());

        Map<String, String> streamData = new HashMap<>();
        streamData.put("voteId", voteId.toString());
        streamData.put("userId", userId.toString());
        streamData.put("optionId", optionId.toString());

        redisTemplate.opsForStream().add(RedisStreamsConfig.VOTE_STREAM_KEY, streamData);

    }

    private VoteInfoDTO getVoteInfoWithType(Long voteId, Long userId) {
        // ... (existing implementation) ...
        String cacheKey = "vote:info:" + voteId;

        String jsonStr = (String) redisTemplate.opsForValue().get(cacheKey);

        if (jsonStr != null) {
            // ...
            try {
                return objectMapper.readValue(jsonStr, VoteInfoDTO.class);
            } catch (Exception e) {
                log.error("레디스 파싱 에러", e);
            }
        }

        // ...

        VoteEntity vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new IllegalArgumentException("투표를 찾을 수 없습니다."));

        VoteInfoDTO info = VoteInfoDTO.toDTO(vote);

        try {
            String jsonValue = objectMapper.writeValueAsString(info);
            redisTemplate.opsForValue().set(cacheKey, jsonValue, Duration.ofDays(1));
        } catch (Exception e) {
            log.error("레디스 시얼라이즈 에러", e);
        }

        return info;
    }

    private VoteStatsDTO getVoteStatus(VoteEntity vote) {
        Long voteId = vote.getId();
        String cacheKey = "vote:stats:" + voteId;
        Map<Object, Object> rawMap = redisTemplate.opsForHash().entries(cacheKey);

        // Redis에 데이터가 없으면 DB에서 복원
        if (rawMap == null || rawMap.isEmpty()) {
            log.info("🐢 Stats Cache MISS.. (DB에서 복원): {}", voteId);
            restoreVoteStats(vote);
            // 복원 후 다시 조회
            rawMap = redisTemplate.opsForHash().entries(cacheKey);
        } else {
            log.info("🔥 Stats Cache HIT!: {}", voteId);
        }

        return VoteStatsDTO.toRedisMap(voteId, rawMap);
    }

    private void restoreVoteStats(VoteEntity vote) {
        String cacheKey = "vote:stats:" + vote.getId();
        Map<String, String> initialStats = new HashMap<>();

        // 1. 총 투표 수
        initialStats.put("total", String.valueOf(vote.getTotalVoteCount()));

        // 2. 옵션별 투표 수
        for (VoteOptionEntity option : vote.getOptions()) {
            initialStats.put("opt:" + option.getId(), String.valueOf(option.getCount()));
        }

        // TODO: 세부 통계(MBTI 등)는 VoteRecord 전체 집계가 필요하므로 일단 생략하거나 비동기 처리
        // 현재는 0으로 시작

        redisTemplate.opsForHash().putAll(cacheKey, initialStats);
    }

    private void incrementStat(String key, Long optionId, String category, Object value) {
        if (value != null) {
            String field = "opt:" + optionId + ":" + category + ":" + value.toString();
            redisTemplate.opsForHash().increment(key, field, 1);
        }
    }

}
