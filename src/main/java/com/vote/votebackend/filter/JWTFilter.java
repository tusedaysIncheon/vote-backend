package com.vote.votebackend.filter;

import com.vote.votebackend.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null) {                    //인가가 없으면 다음필터로 넘김
            filterChain.doFilter(request,response);

            return;
        }

        if (!authorization.startsWith("Bearer ")) {
            log.warn("Invalid Authorization header for URI {} - expected Bearer token", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"유효하지 않은 Authorization 헤더\"}");
            return;
        }

        String accessToken = authorization.split(" ")[1];

        if(JWTUtil.isValid(accessToken, true)){

            String username = JWTUtil.getUsername(accessToken);
            String role = JWTUtil.getRole(accessToken);
            String prefixedRole = role != null && role.startsWith("ROLE_") ? role : "ROLE_" + role;

            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(prefixedRole));

            Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request,response);

        } else {
            log.warn("JWTFilter rejecting token for URI {} - invalid/expired token", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"토큰 만료 또는 유효하지 않은 토큰\"}");
        }
    }
}
