package com.vote.votebackend.domain.user.controller;

import com.vote.votebackend.global.jwt.service.JwtService;
import com.vote.votebackend.global.jwt.service.RedisService;
import com.vote.votebackend.domain.user.entity.UserEntity;
import com.vote.votebackend.domain.user.dto.*;
import com.vote.votebackend.domain.user.repository.UserRepository;
import com.vote.votebackend.domain.user.service.UserService;
import com.vote.votebackend.global.security.custom.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie; // ★ 중요: Cookie 대신 이거 사용
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/user")
@Tag(name = "User API", description = "회원가입, 로그인, 유저 정보 관리 API")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisService redisService;

    @Operation(summary = "회원가입 여부 확인", description = "자체 로그인 관련 회원가입 진행 시 해당 유저가 존재 하는지 확인 API.")
    @PostMapping(value = "/exist", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> exist(
            @Validated(UserRequestDTO.existGroup.class) @RequestBody UserRequestDTO dto) {
        return ResponseEntity.ok(userService.existUser(dto));
    }

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록 API.")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDTO> registerUserApi(
            @Validated(UserRequestDTO.addGroup.class) @RequestBody UserRequestDTO dto) {
        UserResponseDTO userResponse = userService.addUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @Operation(summary = "내 정보 조회", description = "유저정보 불러오기 API.")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public UserResponseDTO userMeApi() {
        return userService.readUser();
    }

    @Operation(summary = "내 정보 수정", description = "회원 정보 수정 API.")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> updateUserApi(
            @Validated(UserRequestDTO.updateGroup.class) @RequestBody UserRequestDTO dto) throws AccessDeniedException {
        return ResponseEntity.ok(userService.updateUser(dto));
    }

    @Operation(summary = "회원탈퇴", description = "회원탈퇴 (JWT RefreshToken remove 및 회원정보 db 삭제) API.")
    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> deleteUserApi(
            @Validated(UserRequestDTO.deleteGroup.class) @RequestBody UserRequestDTO dto) throws AccessDeniedException {
        userService.deleteUser(dto);
        return ResponseEntity.ok(true);
    }

    @Operation(summary = "로그인", description = "로그인 API (SameSite 쿠키 적용)")
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthLoginResponseDTO> loginApi(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletResponse response) {
        // 1. 유저 조회
        UserEntity user = userRepository
                .findByUsernameAndIsLockAndIsSocial(request.getUsername(), false, false)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 2. 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        // 3. 기기 식별자 처리
        String deviceId = request.getDeviceId();
        if (deviceId == null || deviceId.isEmpty()) {
            deviceId = "unknown-device-id";
        }

        // 4. 토큰 발급
        String accessToken = jwtService.createAccessToken(user.getUsername());
        String refreshToken = jwtService.createRefreshToken(user.getUsername());

        // 5. Redis 저장
        jwtService.addRefresh(user.getUsername(), refreshToken, deviceId);

        // 6. 응답 DTO 생성
        UserResponseDTO userDTO = new UserResponseDTO(
                user.getUsername(),
                user.getIsSocial(),
                user.getEmail());

        // Cookie -> ResponseCookie 교체
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .path("/")
                .sameSite("Lax") // 개발 중엔 Lax 필수 (Strict는 도메인이 같아야 함) // TODO: 배포시 Strict로 변경
                .httpOnly(true)
                .secure(false) // TODO: 배포 시 true로 변경 (HTTPS 필수)
                .maxAge(7 * 24 * 60 * 60) // 7일
                .build();

        // addCookie 대신 addHeader 사용
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        log.info("로그인 성공: {}", user.getUsername());

        return ResponseEntity.ok(new AuthLoginResponseDTO(accessToken, userDTO));
    }

    @Operation(summary = "로그아웃", description = "로그아웃 API")
    @PostMapping("/logout")
    public ResponseEntity<Boolean> logoutApi(
            HttpServletResponse response,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody(required = false) Map<String, String> body) {
        String deviceId = (body != null && body.get("deviceId") != null) ? body.get("deviceId") : "unknown-device-id";

        // 1. Redis 삭제
        if (user != null) {
            String username = user.getUsername();
            redisService.deleteRefreshToken(username, deviceId);
        }

        // ★ 수정 포인트: 쿠키 만료 처리도 ResponseCookie 사용
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .path("/")
                .sameSite("Lax") // TODO: 배포시 Strict로 변경
                .httpOnly(true)
                .secure(false) // TODO: 배포 시 true
                .maxAge(0) // 즉시 삭제
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(true);
    }

    @Operation(summary = "유저 총 정보", description = "한 유저모든정보 불러오는 API")
    @GetMapping("/load-info")
    public ResponseEntity<UserDetailsLoadDTO> loadUserInfo(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(userService.getUserDetails(user.getUsername()));
    }

}