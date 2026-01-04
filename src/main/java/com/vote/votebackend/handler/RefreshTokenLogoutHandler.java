//package com.vote.votebackend.handler;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.vote.votebackend.domain.jwt.service.JwtService;
//import com.vote.votebackend.util.JWTUtil;
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.web.authentication.logout.LogoutHandler;
//import org.springframework.util.StreamUtils;
//import org.springframework.util.StringUtils;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//
//@RequiredArgsConstructor
//public class RefreshTokenLogoutHandler implements LogoutHandler {
//
//    private final JwtService jwtService;
//
//    @Override
//    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
//        try {
//
//            String refreshToken = null;
//            String body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
//            if (StringUtils.hasText(body)) {
//                ObjectMapper objectMapper = new ObjectMapper();
//                JsonNode jsonNode = objectMapper.readTree(body);
//                refreshToken = jsonNode.has("refresh_token") ? jsonNode.get("refresh_token").asText() : null;
//            }
//
//            if (refreshToken == null) {
//                Cookie[] cookies = request.getCookies();
//                if (cookies != null) {
//                    for (Cookie cookie : cookies) {
//                        if ("refresh_token".equals(cookie.getName())) {
//                            refreshToken = cookie.getValue();
//                            break;
//                        }
//                    }
//                }
//            }
//
//            if (refreshToken == null) {
//                clearRefreshCookie(response);
//                return;
//            }
//
//            Boolean isValid = JWTUtil.isValid(refreshToken, false);
//
//            if(!isValid) {
//                clearRefreshCookie(response);
//                return;
//            }
//
//            jwtService.removeRefresh(refreshToken);
//
//            clearRefreshCookie(response);
//        } catch (IOException e) {
//            throw new RuntimeException("리프레쉬토큰을 읽어오는데 실패하였습니다.",e);
//        }
//    }
//
//    private void clearRefreshCookie(HttpServletResponse response) {
//        Cookie refreshCookie = new Cookie("refresh_token", null);
//        refreshCookie.setHttpOnly(true);
//        refreshCookie.setSecure(false);
//        refreshCookie.setPath("/");
//        refreshCookie.setMaxAge(0);
//        response.addCookie(refreshCookie);
//    }
//}
