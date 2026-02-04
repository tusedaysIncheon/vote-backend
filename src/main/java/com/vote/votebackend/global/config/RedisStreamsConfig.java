package com.vote.votebackend.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisStreamsConfig {

    private final StringRedisTemplate stringRedisTemplate;

    public static final String VOTE_STREAM_KEY = "vote_stream";
    public static final String VOTE_CUNSUMER_GROUP = "vote_group";

}
