package com.vote.votebackend.domain.vote.controller;

import com.vote.votebackend.domain.vote.dto.VoteInteractDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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

        VoteResponseDTO voteResponse = voteService.createVote(user, dto);

        return ApiResponse.created(voteResponse);
    }

    @GetMapping
    @Operation(summary = "투표 피드 조회", description = "Gravity 알고리즘으로 정렬된 투표 목록을 무한 스크롤 방식으로 조회합니다.")
    public ApiResponse<List<VoteResponseDTO>> loadFeed(
            @RequestParam(defaultValue = "0") int page, // 몇 번째 페이지인지 (0부터 시작)
            @RequestParam(defaultValue = "10") int size // 한 번에 몇 개 가져올지
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<VoteResponseDTO> votePage = voteService.getFeedList(pageable);

        return ApiResponse.ok(votePage.getContent());
    }

    @GetMapping("/{voteId}")
    @Operation(summary = "투표 피드 상세 조회", description = "선택된 투표 상세조회")
    public ApiResponse<VoteResponseDTO> loadVoteDetails(@PathVariable Long voteId) {

        Long userId = SecurityUtils.getCurrentUserId();

        VoteResponseDTO voteResponse = voteService.getVoteDetails(voteId, userId);

        return ApiResponse.ok(voteResponse);

    }

    @PostMapping("/{voteId}")
    @Operation(summary = "투표 옵션 선택", description = "투표의 옵션 선택")
    public ApiResponse<Void> selectOption(
            @PathVariable Long voteId,
            @RequestBody @Valid VoteInteractDTO interactDTO) {
        Long userId = SecurityUtils.getCurrentUserId();

        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        voteService.castVote(voteId, userId, interactDTO.getOptionId());

        return ApiResponse.created(null);

    }

}
