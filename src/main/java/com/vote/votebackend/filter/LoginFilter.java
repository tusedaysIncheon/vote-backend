//package com.vote.votebackend.filter;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.ServletInputStream;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.AuthenticationServiceException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
//import org.springframework.security.web.util.matcher.RequestMatcher;
//import org.springframework.util.StreamUtils;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.Map;
//
//// 로그인 요청을 가로채 처리하는 커스텀 필터. 폼 로그인이 아닌 JSON 바디를 파싱한다.
//public class LoginFilter extends AbstractAuthenticationProcessingFilter {
//
//    // JSON 바디에서 아이디 필드명으로 사용할 키(기본값: "username")
//    public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "username";
//
//    // JSON 바디에서 비밀번호 필드명으로 사용할 키(기본값: "password")
//    public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "password";
//
//    // 이 필터가 매칭할 요청 패턴: POST /login
//    private static final RequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = PathPatternRequestMatcher.withDefaults()
//            .matcher(HttpMethod.POST, "/v1/user/login");
//
//    // 실제로 사용할 username 키(커스터마이즈 가능)
//    private String usernameParameter = SPRING_SECURITY_FORM_USERNAME_KEY;
//
//    // 실제로 사용할 password 키(커스터마이즈 가능)
//    private String passwordParameter = SPRING_SECURITY_FORM_PASSWORD_KEY;
//
//    private final AuthenticationSuccessHandler authenticationSuccessHandler;
//
//    // 필터 생성자: 어떤 AuthenticationManager로 인증을 위임할지 주입
//    public LoginFilter(AuthenticationManager authenticationManager, AuthenticationSuccessHandler authenticationSuccessHandler) {
//        // 부모 생성자에 "이 필터가 언제 동작할지(요청 매처)"와 "인증 매니저"를 전달
//        super(DEFAULT_ANT_PATH_REQUEST_MATCHER, authenticationManager);
//        this.authenticationSuccessHandler = authenticationSuccessHandler;
//    }
//
//    // 필터의 핵심: 매칭된 요청이 오면 인증 시도 로직을 수행
//    @Override
//    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
//            throws AuthenticationException {
//        // POST가 아니면 지원하지 않음(보안/명확성 차원)
//        if (!request.getMethod().equals("POST")) {
//            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
//        }
//
//        // JSON 바디를 파싱해서 담을 맵
//        Map<String, String> loginMap;
//
//        try {
//            // 요청 바디(JSON)를 읽기 위한 ObjectMapper 준비
//            ObjectMapper objectMapper = new ObjectMapper();
//            // HttpServletRequest의 바디 스트림 획득
//            ServletInputStream inputStream = request.getInputStream();
//            // 스트림 → 문자열(UTF-8)로 변환
//            String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
//            // 문자열(JSON) → Map<String, String>으로 역직렬화
//            loginMap = objectMapper.readValue(messageBody, new TypeReference<>() {
//            });
//
//        } catch (IOException e) {
//            // 바디 읽기/파싱 실패 시 런타임 예외로 올려서 실패 처리(인증 실패로 이어짐)
//            throw new RuntimeException(e);
//        }
//
//        // 맵에서 username 키 추출(커스터마이즈 가능한 필드명 사용)
//        String username = loginMap.get(usernameParameter);
//        // 널이면 빈 문자열로, 있으면 공백 제거
//        username = (username != null) ? username.trim() : "";
//
//        // 맵에서 password 키 추출
//        String password = loginMap.get(passwordParameter);
//        // 널이면 빈 문자열로
//        password = (password != null) ? password : "";
//
//        // 아직 인증 전(unauthenticated) 상태의 토큰 생성: 주체(username) + 자격증명(password)
//        UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(username,
//                password);
//
//        // 부가 요청 정보(ip, 세션 ID 등)을 토큰 details에 실어둠(추적/추후 핸들러에서 활용)
//        setDetails(request, authRequest);
//
//        // AuthenticationManager(예: ProviderManager)에게 실제 인증을 위임
//        // → UserDetailsService로 사용자 조회, PasswordEncoder로 비번 매칭 등 진행
//        return this.getAuthenticationManager().authenticate(authRequest);
//    }
//
//    // 토큰 details에 현재 요청의 메타데이터를 심어둔다.
//    protected void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
//        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
//    }
//
//    @Override
//    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
//        authenticationSuccessHandler.onAuthenticationSuccess(request, response, authResult);
//    }
//}