package com.vote.votebackend.domain.user.service;

import com.vote.votebackend.domain.user.entity.UserEntity;
import com.vote.votebackend.domain.user.entity.UserRoleType;
import com.vote.votebackend.domain.user.model.UserRequestDTO;
import com.vote.votebackend.domain.user.repository.UserRepository;
import org.apache.catalina.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.nio.file.AccessDeniedException;

@
@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }


    //자체 로그인 회원 가입 (존재 여부 체크)
    @Transactional(readOnly = true)
    public Boolean existUser(UserRequestDTO dto) {
        return userRepository.existsByUsername(dto.getUserName());
    }

    //자체 로그인 회원 가입
    @Transactional
    public Long addUser(UserRequestDTO dto) {
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

        return userRepository.save(entity).getId();

    }

    //자체 로그인

    //자체 로그인 회원 정보 수정
    @Transactional
    public Long updateUser(UserRequestDTO dto) {

        //본인만 수정 가능 검증
        String sessionUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!sessionUsername.equals(dto.getUsername())) {
            throw new org.springframework.security.access.AccessDeniedException("본인 계정만 수정가능합니다.")
        }
        //조회
        UserEntity entity = userRepository.findByUsernameAndIsLockAndIsSocial(dto.getUsername(), false, false)
                .orElseThrow(() -> new UsernameNotFoundException(dto.getUsername()));

        //회원정보 수정
        entity.updateUser(dto);
        return userRepository.save(entity).getId();

    }

    //자체/소셜 로그인 회원 탈퇴

    //소셜 로그인 ( 매 로그인시: 신규 = 가입, 기존 = 업데이트)

    //자체 /소셜 유저 정보조회

}
