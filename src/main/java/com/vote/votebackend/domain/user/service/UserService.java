package com.vote.votebackend.domain.user.service;

import com.vote.votebackend.domain.jwt.service.JwtService;
import com.vote.votebackend.domain.jwt.service.RedisService;
import com.vote.votebackend.domain.user.entity.SocialProviderType;
import com.vote.votebackend.domain.user.entity.UserEntity;
import com.vote.votebackend.domain.user.entity.UserRoleType;
import com.vote.votebackend.domain.user.model.CustomOAuth2User;
import com.vote.votebackend.domain.user.model.UserRequestDTO;
import com.vote.votebackend.domain.user.model.UserResponseDTO;
import com.vote.votebackend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService extends DefaultOAuth2UserService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RedisService redisService;




    //자체 로그인 회원 가입 (존재 여부 체크)
    @Transactional(readOnly = true)
    public Boolean existUser(UserRequestDTO dto) {
        return userRepository.existsByUsername(dto.getUsername());
    }

    //자체 로그인 회원 가입
    @Transactional
    public UserResponseDTO addUser(UserRequestDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {                 //또 검증하는 이유는 db에 접근하지 않고 거르는 existUser 메서드가 있음에도 불구하고 db에 바로 접근을 하는 경우도 있음
            throw new IllegalArgumentException("이미 유저가 존재합니다.");
        }
        UserEntity entity = UserEntity.builder()                                  //new 생성자로 만들면 @Setter로 접근 가능함(불변성 x) 빌더가 훨씬 간단하고 불변성을 지킬 수 있음.
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .isLock(false)
                .isSocial(false)
                .roleType(UserRoleType.USER)
                .nickname(dto.getNickname())
                .email(dto.getEmail())
                .build();

        UserEntity saved = userRepository.save(entity);

        return new UserResponseDTO(
                saved.getUsername(),
                saved.getIsSocial(),
                saved.getNickname(),
                saved.getEmail(),
                saved.isNeedsNickname()
        );

    }

    //자체 로그인
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity entity = userRepository.findByUsernameAndIsLockAndIsSocial(username, false, false)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        return User.builder()
                .username(entity.getUsername())
                .password(entity.getPassword())
                .roles(entity.getRoleType().name())
                .accountLocked(entity.getIsLock())
                .build();

    }

    //자체 로그인 회원 정보 수정
    @Transactional
    public Long updateUser(UserRequestDTO dto) {

        //본인만 수정 가능 검증
        String sessionUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!sessionUsername.equals(dto.getUsername())) {
            throw new AccessDeniedException("본인 계정만 수정가능합니다.");
        }
        //조회
        UserEntity entity = userRepository.findByUsernameAndIsLockAndIsSocial(dto.getUsername(), false, false)
                .orElseThrow(() -> new UsernameNotFoundException(dto.getUsername()));

        //회원정보 수정
        entity.updateUser(dto);
        return userRepository.save(entity).getId();

    }

    //자체/소셜 로그인 회원 탈퇴
    @Transactional
    public void deleteUser(UserRequestDTO dto) throws AccessDeniedException {
        //본인 또는 어드민만 가능하게 검증 작업
        SecurityContext context = SecurityContextHolder.getContext();
        String sessionUsername = context.getAuthentication().getName();
        String sessionRole = context.getAuthentication().getAuthorities().iterator().next().getAuthority();

        boolean isOwner = sessionUsername.equals(dto.getUsername());
        boolean isAdmin = sessionRole.equals("ROLE_" + UserRoleType.ADMIN.name());

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("본인 혹은 관리자만 삭제할 수 있습니다.");
        }

        //Refresh 토큰 제거 (참조 제약 or 캐시 일관성 문제로 먼저 삭제)
        redisService.deleteAllRefreshTokens(dto.getUsername());

        //유저 삭제
        userRepository.deleteByUsername(dto.getUsername());


    }


    //소셜 로그인 ( 매 로그인시: 신규 = 가입, 기존 = 업데이트)
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        //DefaultOAuth2UserService 의 loadUser 메소드 오버라이드
        //부모 메서드 호출
        OAuth2User oAuth2User = super.loadUser(userRequest);

        //데이터
        Map<String, Object> attributes;
        List<GrantedAuthority> authorities;

        String username;
        String role = UserRoleType.USER.name();
        String email;


        //provider 제공자 별 데이터 획득 ( 제공자 마다 데이터 제공 방법이 다름)

        String registrationId = userRequest.getClientRegistration().getRegistrationId().toUpperCase();


        if (registrationId.equals(SocialProviderType.NAVER.name())) {

            attributes = (Map<String, Object>) oAuth2User.getAttributes().get("response");
            username = registrationId + "_" + attributes.get("id");
            email = attributes.get("email").toString();

        } else if (registrationId.equals(SocialProviderType.GOOGLE.name())) {

            attributes = (Map<String, Object>) oAuth2User.getAttributes();
            username = registrationId + "_" + attributes.get("sub");
            email = attributes.get("email").toString();

        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다.");
        }

        // 데이터베이스 조회 -> 존재하면 업데이트, 없으면 신규 가입
        Optional<UserEntity> entity = userRepository.findByUsernameAndIsSocial(username, true);

        if (entity.isPresent()) {
            //role 조회
            role = entity.get().getRoleType().name();
        } else {
            // 신규 유저 추가
            UserEntity userEntity = UserEntity.builder()
                    .username(username)
                    .password("")
                    .isLock(false)
                    .isSocial(true)
                    .socialProviderType(SocialProviderType.valueOf(registrationId))
                    .roleType(UserRoleType.USER)
                    .needsNickname(true)
                    .email(email)
                    .build();

            userRepository.save(userEntity);
        }

        authorities = List.of(new SimpleGrantedAuthority(role));

        return new CustomOAuth2User(attributes, authorities, username);

    }


    //자체 /소셜 유저 정보조회
    @Transactional(readOnly = true)
    public UserResponseDTO readUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if(username == null){
            throw new AccessDeniedException("존재하지 않는 유저입니다.");
        }

        UserEntity entity = userRepository.findByUsernameAndIsLock(username, false)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다" + username));

        return new UserResponseDTO(username, entity.getIsSocial(), entity.getNickname(), entity.getEmail(), entity.isNeedsNickname());
    }

    @Transactional
    public UserResponseDTO updateNickname(String username, String nickname) {
        if (username == null) {
            throw new UsernameNotFoundException("인증 정보가 유효하지 않습니다.");
        }

        userRepository.updateNicknameByUsername(username, nickname);

        UserEntity saved = userRepository.findByUsernameAndIsSocial(username, true)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        return new UserResponseDTO(
                saved.getUsername(),
                saved.getIsSocial(),
                saved.getNickname(),
                saved.getEmail(),
                saved.isNeedsNickname()
        );
    }
}
