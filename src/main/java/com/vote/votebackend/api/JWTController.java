package com.vote.votebackend.api;


import com.vote.votebackend.domain.jwt.model.JWTResponseDTO;
import com.vote.votebackend.domain.jwt.model.RefreshRequestDTO;
import com.vote.votebackend.domain.jwt.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class JWTController {

    private final JwtService jwtService;

    //소셜 로그인 방식 RefreshToken 헤더를 이용 Token 교환
    @PostMapping(value = "/jwt/exchange", consumes = MediaType.APPLICATION_JSON_VALUE)
    public JWTResponseDTO jwtExchangeAPI(HttpServletResponse response, HttpServletRequest request) {

        return jwtService.cookie2token(response, request);
    }

    //Refresh 토큰으로 Access 토큰 재발급
    @PostMapping(value = "/jwt/refresh")
    public JWTResponseDTO jwtRefreshAPI(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName()) || "refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        // 2. 쿠키 값 검증 (Validation)
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("리프레시 토큰이 쿠키에 없습니다.");
            // 또는 커스텀 예외 처리
        }

        // 3. [핵심] 서비스에 넘겨주기 위해 DTO를 수동으로 생성 (어댑터 역할)
        RefreshRequestDTO dto = new RefreshRequestDTO();
        dto.setRefreshToken(refreshToken);

        // 4. 기존 서비스 로직 그대로 호출 (서비스는 DTO가 쿠키에서 왔는지 모름)
        return jwtService.refreshRotate(dto, response);
    }


}
