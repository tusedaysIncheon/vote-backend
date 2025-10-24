package com.vote.votebackend.config;

import com.vote.votebackend.LoginFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration) {
        this.authenticationConfiguration = authenticationConfiguration;
    }
    // 커스텀 자체 로그인 필터를 위한 AuthenticationManager Bean 수동 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // 비밀번호 단방향(BCrypt) 암호화용 Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //securityFilterChain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        //CSRF 보안필터 x
        http
                .csrf(AbstractHttpConfigurer::disable);

        //CORS FE와 BE가 다른 오리진을 가진 경우 셋팅해야함 (react + spring)


        // 기본 form 기반 인증 필터 disable
        http
                .formLogin(AbstractHttpConfigurer::disable);

        //기본 basic 인증 필터 disable ->

        http
                .httpBasic(AbstractHttpConfigurer::disable);

        //인가

        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll());

        http
                .exceptionHandling(e -> e.authenticationEntryPoint((request, response, authException) -> {
                                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                                })
                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                                })

                );

        // 커스텀 필터 추가
        http
                .addFilterBefore(new LoginFilter(authenticationManager(authenticationConfiguration)), UsernamePasswordAuthenticationFilter.class);

        //세션필터 설정 -> JWT 방식은 세션을 꺼야됌 클라이언트의 세션정보를 서버에 저장하지않음 토큰을 계속 확인하면서 처리하는 API

        http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));



        return http.build();


    }

}
