package com.vote.votebackend.global.security.service;

import com.vote.votebackend.domain.user.dto.CustomOAuth2User;
import com.vote.votebackend.domain.user.entity.UserEntity;
import com.vote.votebackend.domain.user.entity.enums.SocialProviderType;
import com.vote.votebackend.domain.user.entity.enums.UserRoleType;
import com.vote.votebackend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @SuppressWarnings("unchecked")
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes;
        String username;
        String email;
        String role = UserRoleType.USER.name();
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
            throw new OAuth2AuthenticationException("Unsupported social login provider: " + registrationId);
        }

        UserEntity userEntity;

        Optional<UserEntity> entity = userRepository.findByUsernameAndIsSocial(username, true);

        if (entity.isPresent()) {
            userEntity = entity.get();
            role = userEntity.getRoleType().name();
        } else {
            userEntity = UserEntity.builder()
                    .username(username)
                    .password("") // 소셜 유저는 비밀번호 없음
                    .isLock(false)
                    .isSocial(true) // 수정: 소셜 유저이므로 true
                    .socialProviderType(SocialProviderType.valueOf(registrationId))
                    .roleType(UserRoleType.USER)
                    .email(email)
                    .build();
            userRepository.save(userEntity);
        }
        return new CustomOAuth2User(attributes, List.of(new SimpleGrantedAuthority(role)), username, userEntity);
    }

}
