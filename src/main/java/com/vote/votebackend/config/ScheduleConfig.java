package com.vote.votebackend.config;

import com.vote.votebackend.domain.jwt.repository.RefreshRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ScheduleConfig {
    private final RefreshRepository refreshRepository;

    //8일 지난 토큰 삭제
    @Scheduled(cron = "0 0 3 * * *")
    public void oldRefreshEntityRemove() {
        LocalDateTime cutoff =  LocalDateTime.now().minusDays(8);
        refreshRepository.deleteByCreatedDateBefore(cutoff);
    }

}
