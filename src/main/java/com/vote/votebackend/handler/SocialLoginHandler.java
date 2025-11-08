package com.vote.votebackend.handler;

import com.vote.votebackend.domain.jwt.repository.RefreshRepository;
import com.vote.votebackend.domain.jwt.service.JwtService;
import com.vote.votebackend.util.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Qualifier("SocialSuccessHandler")
@RequiredArgsConstructor
public class SocialLoginHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final RefreshRepository refreshRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        //username , role 파싱
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        //JWT 발급
        String refreshToken = JWTUtil.createJWT(username, "ROLE_" + role, false);

        String deviceId = request.getHeader("User-Agent");
        if (deviceId == null || deviceId.isEmpty()) {
            deviceId = "unknown-device";
        }

        jwtService.removeRefreshByUsernameAndDeviceId(username,deviceId);

        //발급한 리프레쉬 토큰 DB 테이블 저장
        jwtService.addRefresh(username, refreshToken, deviceId);

        //응답
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(10);

        response.addCookie(refreshCookie); // 10초 (프론트에서 발급 후 바로 헤더 전환 예정)
        response.sendRedirect("http://localhost:5173/cookie"); //프론트 주소 -> 포트번호 확인후 변경!


    }
}
