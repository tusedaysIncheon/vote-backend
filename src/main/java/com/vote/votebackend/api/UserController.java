package com.vote.votebackend.api;

import com.vote.votebackend.domain.jwt.service.JwtService;
import com.vote.votebackend.domain.jwt.service.RedisService;
import com.vote.votebackend.domain.user.entity.UserEntity;
import com.vote.votebackend.domain.user.model.*;
import com.vote.votebackend.domain.user.repository.UserRepository;
import com.vote.votebackend.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/user")
@Tag(name = "user", description = "회원가입 및 유저 정보 수정삭제 API")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisService redisService;

    // 자체 로그인 유저 존재 확인
    @Operation(summary = "회원가입", description = "자체 로그인 관련 회원가입 진행 시 해당 유저가 존재 하는지 확인 API.")
    @PostMapping(value = "/exist", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> exist(
            @Validated(UserRequestDTO.existGroup.class) @RequestBody UserRequestDTO dto
    ) {
        return ResponseEntity.ok(userService.existUser(dto));
    }


    // 회원가입
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록 API.")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDTO> registerUserApi(
            @Validated(UserRequestDTO.addGroup.class) @RequestBody UserRequestDTO dto
    ) {

        UserResponseDTO userResponse = userService.addUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);

    }

    // 유저 정보 불러오기
    @Operation(summary = "회원정보", description = "유저정보 불러오기 API.")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public UserResponseDTO userMeApi() {

        return userService.readUser();
    }

    // 유저 수정 (자체 로그인 유저만)
    @Operation(summary = "회원정보", description = "회원 정보 수정 API.")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> updateUserApi(
            @Validated(UserRequestDTO.updateGroup.class) @RequestBody UserRequestDTO dto
    ) throws AccessDeniedException {

        return ResponseEntity.status(200).body(userService.updateUser(dto));
    }


    // 유저 제거 (자체/소셜)
    @Operation(summary = "회원탈퇴", description = "회원탈퇴 (JWT RefreshToken remove 및 회원정보 db 삭제) API.")
    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> deleteUserApi(
            @Validated(UserRequestDTO.deleteGroup.class) @RequestBody UserRequestDTO dto
    ) throws AccessDeniedException {
        userService.deleteUser(dto);
        return ResponseEntity.status(200).body(true);
    }

    //유저 닉네임만 따로받는 API
    @Operation(summary = "회원가입", description = "회원가입 시 닉네임 따로받는 API")
    @PatchMapping("/nickname")
    public ResponseEntity<UserResponseDTO> updateNicknameApi(
            @Valid @RequestBody NicknameUpdateRequestDTO request, @AuthenticationPrincipal String username) {

        UserResponseDTO response = userService.updateNickname(username, request.nickname());
        return ResponseEntity.ok(response);
    }

    //로그인 API
    @Operation(summary = "로그인", description = "로그인 API")
    @PostMapping(value="/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthLoginResponseDTO>loginApi(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletResponse response

    ){
        // 1) 아이디로 '일반' 유저 조회 (isLock=false, isSocial=false)
        //    - 소셜 유저는 이 API가 아니라 OAuth 플로우로 로그인하므로 제외
        UserEntity user = userRepository
                .findByUsernameAndIsLockAndIsSocial(request.getUsername(),false,false)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        // 4️⃣ 기기 식별자 (User-Agent 기반 or 프론트 전달값)
        String deviceId = request.getDeviceId(); // <— 새 필드 추가
        if (deviceId == null || deviceId.isEmpty()) {
            deviceId = "unknown-device";
        }

        String accessToken = jwtService.createAccessToken(user.getUsername());
        String refreshToken = jwtService.createRefreshToken(user.getUsername());


        jwtService.addRefresh(user.getUsername(),refreshToken, deviceId);

        UserResponseDTO userDTO = new UserResponseDTO(
                user.getUsername(),
                user.getIsSocial(),
                user.getNickname(),
                user.getEmail(),
                user.isNeedsNickname()
        );

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false); // TODO : 추후 배포시 true로 변경 예정
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshCookie);

        AuthLoginResponseDTO responseBody = new AuthLoginResponseDTO(
                accessToken,
                userDTO
        );

            return ResponseEntity.ok(responseBody);
    }

    @Operation(summary = "로그아웃", description = "로그아웃 API (Redis 토큰 삭제 및 쿠키 만료)")
    @PostMapping("/logout")
    public ResponseEntity<Boolean> logoutApi(
            HttpServletResponse response,
            @AuthenticationPrincipal String username,
            @RequestBody(required = false)Map<String, String> body
            ){
        String deviceId = (body != null) ? body.get("deviceId") : "unknown-device";

        redisService.deleteRefreshToken(username,deviceId);

        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // TODO : 추후 배포시 true로 변경 예정
        response.addCookie(cookie);

        return ResponseEntity.ok(true);

    }


}
