package com.vote.votebackend.domain.vote.controller;

import com.vote.votebackend.domain.user.entity.UserEntity;
import com.vote.votebackend.domain.vote.dto.VoteRequestDTO;
import com.vote.votebackend.domain.vote.dto.VoteResponseDTO;
import com.vote.votebackend.domain.vote.service.VoteService;
import com.vote.votebackend.global.exception.InvalidTokenException;
import com.vote.votebackend.global.util.ApiResponse;
import com.vote.votebackend.global.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/vote")
@Tag(name = "Vote API", description = "투표 관련 API")
public class VoteController {

    private final VoteService voteService;

    @PostMapping
    @Operation(summary = "투표 생성", description = "제목, 내용, 마감일, 옵션 등을 받아 투표를 생성합니다.")
    public ApiResponse<VoteResponseDTO> newVote(@Valid @RequestBody VoteRequestDTO dto) {

        Long user = SecurityUtils.getCurrentUserId();

        if (user == null) {
            throw new InvalidTokenException("로그인이 필요한 서비스입니다.");
        }

        VoteResponseDTO voteResponse = voteService.addVote(user,dto);

        return ApiResponse.created(voteResponse);
    }

}
