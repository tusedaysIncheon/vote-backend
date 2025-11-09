package com.vote.votebackend.api;


import com.vote.votebackend.domain.jwt.model.JWTResponseDTO;
import com.vote.votebackend.domain.jwt.model.RefreshRequestDTO;
import com.vote.votebackend.domain.jwt.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    @PostMapping(value = "/jwt/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
    public JWTResponseDTO jwtRefreshAPI(@Validated @RequestBody RefreshRequestDTO dto, HttpServletResponse response) {

        return jwtService.refreshRotate(dto, response);
    }


}
