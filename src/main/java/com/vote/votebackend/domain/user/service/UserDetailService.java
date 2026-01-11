package com.vote.votebackend.domain.user.service;

import com.vote.votebackend.domain.user.entity.UserDetailsEntity;
import com.vote.votebackend.domain.user.entity.UserEntity;
import com.vote.votebackend.domain.user.model.UserDetailsLoadDTO;
import com.vote.votebackend.domain.user.model.UserDetailsRequestDTO;
import com.vote.votebackend.domain.user.model.UserDetailsResponseDTO;
import com.vote.votebackend.domain.user.repository.UserDetailsRepository;
import com.vote.votebackend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDetailService {

    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;

    @Transactional
    public void saveUserDetails(String username, UserDetailsRequestDTO dto) {

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 사용자입니다."));

        UserDetailsEntity existingDetail = userDetailsRepository.findByUser(user).orElse(null);

        if(existingDetail == null || !existingDetail.getNickname().equals(dto.getNickname())){
            if(userDetailsRepository.existsByNickname(dto.getNickname())){
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }

            if(existingDetail == null){
                UserDetailsEntity newDetail = UserDetailsEntity.builder()
                        .user(user)
                        .nickname(dto.getNickname())
                        .birthYear(dto.getBirthYear())
                        .gender(dto.getGender())
                        .region(dto.getRegion())
                        .mbti(dto.getMbti())
                        .introduce(dto.getIntroduce())
                        .imageUrl(dto.getImageUrl())
                        .relationshipStatus(dto.getRelationshipStatus())
                        .build();

                userDetailsRepository.save(newDetail);
            } else {
                existingDetail.updateProfile(
                        dto.getNickname(),
                        dto.getImageUrl(),
                        dto.getIntroduce(),
                        dto.getMbti(),
                        dto.getRelationshipStatus(),
                        dto.getRegion(),
                        dto.getBirthYear(),
                        dto.getGender()
                );
            }
        }
    }

    public UserDetailsResponseDTO getProfile(String username){

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 사용자입니다."));

        UserDetailsEntity userDetails = userDetailsRepository.findByUser(user)
                .orElseThrow(()->new IllegalArgumentException("프로필이 설정되지 않았습니다."));

        return new UserDetailsResponseDTO(userDetails);

    }

    public Boolean existNickname(String nickname) {
        return userDetailsRepository.existsByNickname(nickname);
    }




}
