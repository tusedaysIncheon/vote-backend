package com.vote.votebackend.domain.jwt.service;

import com.vote.votebackend.domain.jwt.entity.RefreshEntity;
import com.vote.votebackend.domain.jwt.model.JWTResponseDTO;
import com.vote.votebackend.domain.jwt.model.RefreshRequestDTO;
import com.vote.votebackend.domain.jwt.repository.RefreshRepository;
import com.vote.votebackend.domain.user.entity.UserRoleType;
import com.vote.votebackend.domain.user.repository.UserRepository;
import com.vote.votebackend.util.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JwtService {

    private final RefreshRepository refreshRepository;
    private final UserRepository userRepository;

    public JwtService(RefreshRepository refreshRepository, UserRepository userRepository) {
        this.refreshRepository = refreshRepository;
        this.userRepository = userRepository;
    }

    // 소셜 로그인 성공 후 쿠키(Refresh) -> 헤더 방식으로 응답
    @Transactional
    public JWTResponseDTO cookie2token(HttpServletResponse response, HttpServletRequest request) {

        //쿠키가 있는지 검증
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new RuntimeException("쿠키가 존재하지 않습니다.");
        }

        //refresh 토큰 획득
        String refreshToken = null;
//        for (Cookie cookie : cookies) {
//            if (cookie.getName().equals("refresh_token")) {
//                refreshToken = cookie.getValue();
//            }
//        }
        //자바에서 리터럴 비교 시 문자열이 앞으로 오게해서 비교해야함 -> NPE 방지
        for (Cookie cookie : cookies) {
            if ("refresh_token".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }

        if (refreshToken == null) {
            throw new RuntimeException("리프레쉬 토큰이 확인되지 않습니다.");
        }

        //refresh 토큰 검증

        Boolean isValid = JWTUtil.isValid(refreshToken, false);
        if (!isValid) {
            throw new RuntimeException("유효하지 않은 리프레쉬 토큰입니다.");
        }

        //유저 정보 추출
        String username = JWTUtil.getUsername(refreshToken);
        String role = JWTUtil.getRole(refreshToken);

        String deviceId = request.getHeader("Device-Id");
        if (deviceId == null || deviceId.isBlank()) {
            deviceId = request.getHeader("User-Agent"); // fallback
        }

        //새 토큰 발급
        String newAccessToken = JWTUtil.createJWT(username, role, true);
        String newRefreshToken = JWTUtil.createJWT(username, role, false);


        RefreshEntity newRefreshEntity = RefreshEntity.builder()
                .username(username)
                .deviceId(deviceId)
                .refresh(newRefreshToken)
                .build();

        removeRefreshByUsernameAndDeviceId(username,deviceId);
        refreshRepository.flush();
        refreshRepository.save(newRefreshEntity);

        //기존 쿠키 제거
        Cookie refreshCookie = new Cookie("refresh_token", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(10);
        response.addCookie(refreshCookie);

        return new JWTResponseDTO(newAccessToken, newRefreshToken);

    }



    // Refresh 토큰으로 Access 토큰 재발급 로직 (Rotate 포함)
    @Transactional
    public JWTResponseDTO refreshRotate(RefreshRequestDTO dto) {

        String refreshToken = dto.getRefreshToken();

        Boolean isValid = JWTUtil.isValid(refreshToken, false);
        if (!isValid) {
            throw new RuntimeException("유효하지 않은 리프레쉬 토큰입니다.");
        }

        if (!existsRefresh(refreshToken)) {
            throw new RuntimeException("유효하지 않은 리프레쉬 토큰입니다.");
        }

        String username = JWTUtil.getUsername(refreshToken);
        String role = JWTUtil.getRole(refreshToken);

        String deviceId = refreshRepository.findByRefresh(refreshToken)
                .map(RefreshEntity::getDeviceId)
                .orElse("unknown-device");

        String newAccessToken = JWTUtil.createJWT(username, role, true);
        String newRefreshToken = JWTUtil.createJWT(username, role, false);

        RefreshEntity newRefreshEntity = RefreshEntity.builder()
                .username(username)
                .refresh(newRefreshToken)
                .build();

        removeRefresh(refreshToken);
        refreshRepository.save(newRefreshEntity);

        return new JWTResponseDTO(newAccessToken, newRefreshToken);
    }

    // JWT Refresh 토큰 발급 후 저장 메소드
    @Transactional
    public void addRefresh(String username, String refreshToken, String deviceId) {
       removeRefreshByUsernameAndDeviceId(username,deviceId);

        RefreshEntity entity = RefreshEntity.builder()
                .username(username)
                .deviceId(deviceId)
                .refresh(refreshToken)
                .build();

        refreshRepository.save(entity);
    }


    // JWT Refresh 존재 확인 메소드

    @Transactional(readOnly = true)
    public Boolean existsRefresh(String refreshToken) {
        return refreshRepository.existsByRefresh(refreshToken);
    }

    // JWT Refresh 토큰 삭제 메소드
    // 레포지토리 커스텀 JPA 에 트랜잭셔널 선언을 이미했음.

    public void removeRefresh(String refreshToken) {
        refreshRepository.deleteByRefresh(refreshToken);
    }

    // 특정 유저 Refresh 토큰 모두 삭제 (탈퇴)

    public void removeRefreshUser(String username) {
        refreshRepository.deleteByUsername(username);
    }

    // 로그인 시 access 토큰 발급 메소드
    public String createAccessToken(String username) {
        String role = userRepository.findRoleTypeByUsername(username)
                .map(Enum :: name)
                .orElse(UserRoleType.USER.name());

        return JWTUtil.createJWT(username, role, true);
    }

    //로그인 시 refresh 토큰 발급 메소드
    public String createRefreshToken(String username) {
        String role = userRepository.findRoleTypeByUsername(username)
                .map(Enum::name)
                .orElse(UserRoleType.USER.name());

        return JWTUtil.createJWT(username, role, false);
    }

    public void removeRefreshByUsernameAndDeviceId(String username, String deviceId) {
        refreshRepository.deleteByUsernameAndDeviceId(username, deviceId);
    }


}

