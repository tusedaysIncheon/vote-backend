package com.vote.votebackend.domain.vote.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class VoteOptionStatusDTO {

    private Map<String, Long> mbti;
    private Map<String, Long> age;
    private Map<String, Long> region;
    private Map<String, Long> status;

}
