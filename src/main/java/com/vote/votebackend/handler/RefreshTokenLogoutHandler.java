package com.vote.votebackend.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vote.votebackend.domain.jwt.service.JwtService;
import com.vote.votebackend.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class RefreshTokenLogoutHandler implements LogoutHandler {

    private final JwtService jwtService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {

//            String body = new BufferedReader(new InputStreamReader((request.getInputStream())))
//                    .lines().reduce("", String::concat);

            String body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            if (!StringUtils.hasText(body)) return;

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(body);
            String refreshToken = jsonNode.has("refresh_token") ? jsonNode.get("refresh_token").asText() : null;

            if (refreshToken == null) return;

            Boolean isValid = JWTUtil.isValid(refreshToken, false);

            if(!isValid) return;

            jwtService.removeRefresh(refreshToken);

        } catch (IOException e) {
            throw new RuntimeException("리프레쉬토큰을 읽어오는데 실패하였습니다.",e);
        }
    }
}
