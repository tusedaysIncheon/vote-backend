package com.vote.votebackend.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisStreamsConfig {

    // Key Convention: 도메인:리소스:용도
    public static final String VOTE_STREAM_KEY = "vote:stream:log";
    public static final String VOTE_CONSUMER_GROUP = "vote-group";

}