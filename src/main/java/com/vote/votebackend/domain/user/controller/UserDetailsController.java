package com.vote.votebackend.domain.user.controller;

import com.vote.votebackend.domain.user.dto.UserDetailsRequestDTO;
import com.vote.votebackend.domain.user.dto.UserDetailsResponseDTO;
import com.vote.votebackend.domain.user.service.UserDetailService;
import com.vote.votebackend.global.util.ApiResponse;
import com.vote.votebackend.global.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/user-details")
@RequiredArgsConstructor
public class UserDetailsController {

    private final UserDetailService userDetailService;

    @PostMapping
    public ApiResponse<String> saveUserDetails(
            @Valid @RequestBody UserDetailsRequestDTO dto) {
        // @AuthenticationPrincipal CustomUserDetails user removed

        String username = SecurityUtils.getCurrentUser().getUsername();
        if (username == null) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }

        log.info("save user details : {}", username);

        userDetailService.saveUserDetails(username, dto);

        return ApiResponse.ok("프로필이 성공적으로 저장되었습니다.");
    }

    @GetMapping
    public ApiResponse<UserDetailsResponseDTO> getMyInfo() {
        // @AuthenticationPrincipal CustomUserDetails user removed

        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }

        log.info("get user details : {}", userId);

        UserDetailsResponseDTO profile = userDetailService.getProfile(userId);
        return ApiResponse.ok(profile);
    }

    @GetMapping("/exist-nickname")
    public ApiResponse<Boolean> existNickname(@RequestParam String nickname) {
        log.info("exist nickname : {}", nickname);
        return ApiResponse.ok(userDetailService.existNickname(nickname));

    }

}
