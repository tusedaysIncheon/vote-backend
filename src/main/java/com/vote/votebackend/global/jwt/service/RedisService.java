package com.vote.votebackend.global.jwt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 리프레시 토큰 저장 (Save)
     * @param username 사용자 아이디
     * @param deviceId 기기 식별자 (UUID)
     * @param refreshToken 저장할 토큰 값
     * @param durationInMillis 토큰 유효 시간 (밀리초)
     */
    public void setRefreshToken(String username,String deviceId, String refreshToken, Long durationInMillis){

        String key = "RT:" + username + ":" + deviceId;

        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, refreshToken, Duration.ofMillis(durationInMillis));

    }

    /**
     * 리프레시 토큰 조회 (Get)
     * @return 저장된 토큰 값 (없으면 null)
     */
    public String getRefreshToken(String username, String deviceId){
        String key = "RT:" + username + ":" + deviceId;

        return (String)redisTemplate.opsForValue().get(key);
    }

    /**
     * 리프레시 토큰 삭제 (Delete)
     * 로그아웃 시 사용
     */
    public void deleteRefreshToken(String username, String deviceId){
        String key = "RT:" + username + ":" + deviceId;

        redisTemplate.delete(key);
    }

    public void deleteAllRefreshTokens(String username){
        Set<String> keys =  redisTemplate.keys("RT:" + username + ":*");

        if ( keys != null && !keys.isEmpty() ){
            redisTemplate.delete(keys);
        }
    }

}
