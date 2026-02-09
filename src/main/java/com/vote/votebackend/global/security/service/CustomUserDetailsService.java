package com.vote.votebackend.global.security.service;

import com.vote.votebackend.domain.user.entity.UserEntity;
import com.vote.votebackend.global.security.custom.CustomUserDetails;
import com.vote.votebackend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity entity = userRepository.findByUsernameAndIsLock(username, false)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new CustomUserDetails(entity);
    }
}
