package com.vote.votebackend.api;

import com.vote.votebackend.domain.user.model.UserRequestDTO;
import com.vote.votebackend.domain.user.model.UserResponseDTO;
import com.vote.votebackend.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/user")
@Tag(name = "user", description = "회원가입 및 유저 정보 수정삭제 API")
public class UserController {

    private final UserService userService;

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
    public ResponseEntity<Map<String, Long>> registerUserApi(
            @Validated(UserRequestDTO.addGroup.class) @RequestBody UserRequestDTO dto
    ) {

        Long id = userService.addUser(dto);
        Map<String, Long> responseBody = Collections.singletonMap("userEntityId", id);
        return ResponseEntity.status(201).body(responseBody);

    }

    // 유저 정보 불러오기
    @Operation(summary = "회원정보", description = "유저정보 불러오기 API.")
    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
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


}
