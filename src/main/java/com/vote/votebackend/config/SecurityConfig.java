package com.vote.votebackend.config;

import com.vote.votebackend.domain.jwt.service.JwtService;
import com.vote.votebackend.domain.user.entity.enums.UserRoleType;
import com.vote.votebackend.filter.JWTFilter;
import com.vote.votebackend.filter.RateLimitFilter;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final AuthenticationSuccessHandler loginSuccessHandler;
    private final AuthenticationSuccessHandler SocialLoginSuccessHandler;
    private final JwtService jwtService;
    private final LettuceBasedProxyManager<byte[]> proxyManager;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration,
                          @Qualifier("LoginSuccessHandler") AuthenticationSuccessHandler loginSuccessHandler,
                          @Qualifier("SocialSuccessHandler") AuthenticationSuccessHandler socialLoginSuccessHandler, JwtService jwtService, LettuceBasedProxyManager<byte[]> proxyManager, RedisTemplate<String, Object> redisTemplate) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.loginSuccessHandler = loginSuccessHandler;
        this.SocialLoginSuccessHandler = socialLoginSuccessHandler;
        this.jwtService = jwtService;
        this.proxyManager = proxyManager;
        this.redisTemplate = redisTemplate;
    }

    // 커스텀 자체 로그인 필터를 위한 AuthenticationManager Bean 수동 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    //권한 계층 나누는 Bean
    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withRolePrefix("ROLE_")
                .role(UserRoleType.ADMIN.name()).implies(UserRoleType.USER.name())
                .build();
    }

    // 비밀번호 단방향(BCrypt) 암호화용 Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //CORS 설정
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    //securityFilterChain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        //CSRF 보안필터 x
        http
                .csrf(AbstractHttpConfigurer::disable);

        //CORS 설정
        //FE와 BE가 다른 오리진을 가진 경우 셋팅해야함 (react + spring)
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));
        // 기본 로그아웃 필터 + 커스텀 Refresh 토큰 삭제 핸들러 추가
//        http
//                .logout(logout -> logout
//                        .addLogoutHandler(new RefreshTokenLogoutHandler(jwtService)));
//

        // 기본 form 기반 인증 필터 disable
        http
                .formLogin(AbstractHttpConfigurer::disable);

        //기본 basic 인증 필터 disable ->

        http
                .httpBasic(AbstractHttpConfigurer::disable);

        http
                .requestCache(cache -> cache
                        .requestCache(new NullRequestCache()));

        //OAuth2 인증용
        http
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(SocialLoginSuccessHandler));

        //인가

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/jwt/refresh", "/jwt/exchange").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/user/exist", "/v1/user", "/v1/user/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/user").hasRole(UserRoleType.USER.name())
                        .requestMatchers(HttpMethod.PUT, "/v1/user").hasRole(UserRoleType.USER.name())
                        .requestMatchers(HttpMethod.DELETE, "/v1/user").hasRole(UserRoleType.USER.name())
                        .requestMatchers(HttpMethod.PATCH, "/v1/user/nickname").hasRole(UserRoleType.USER.name())
                        .anyRequest().authenticated()
                );
        http
                .exceptionHandling(e -> e.authenticationEntryPoint((request, response, authException) -> {
                                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                                })
                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                                })

                );


        //bucket 필터 추가
        http
                .addFilterBefore(new RateLimitFilter(proxyManager, redisTemplate)
                , UsernamePasswordAuthenticationFilter.class);

        // 커스텀 필터 추가
        http
                .addFilterBefore(new JWTFilter(), UsernamePasswordAuthenticationFilter.class);

//        http
//                .addFilterBefore(new LoginFilter(authenticationManager(authenticationConfiguration), loginSuccessHandler), UsernamePasswordAuthenticationFilter.class);

        http
                .securityContext(context -> context
                        .securityContextRepository(new NullSecurityContextRepository()));

        //세션필터 설정 -> JWT 방식은 세션을 꺼야됌 클라이언트의 세션정보를 서버에 저장하지않음 토큰을 계속 확인하면서 처리하는 API

        http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .sessionFixation(sessionFixation -> sessionFixation.none()));


        return http.build();


    }

}
