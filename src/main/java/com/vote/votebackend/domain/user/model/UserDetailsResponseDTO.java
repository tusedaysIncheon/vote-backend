package com.vote.votebackend.domain.user.model;

import com.vote.votebackend.domain.user.entity.UserDetailsEntity;
import com.vote.votebackend.domain.user.entity.enums.Gender;
import com.vote.votebackend.domain.user.entity.enums.MBTI;
import com.vote.votebackend.domain.user.entity.enums.Region;
import com.vote.votebackend.domain.user.entity.enums.RelationshipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Year;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsResponseDTO {


    private Long id;
    private String nickname;
    private String imageUrl; // S3 URL
    private String introduce;

    private Integer birthYear;
    private Integer age;
    private Gender gender;
    private MBTI mbti;
    private Region region;
    private RelationshipStatus relationshipStatus;

    public UserDetailsResponseDTO(UserDetailsEntity entity) {
        this.id = entity.getId();
        this.nickname = entity.getNickname();
        this.imageUrl = entity.getImageUrl();
        this.introduce = entity.getIntroduce();
        this.birthYear = entity.getBirthYear();

        // 나이 계산
        int currentYear = Year.now().getValue();
        this.age = currentYear - entity.getBirthYear() + 1;

        this.gender = entity.getGender();
        this.region = entity.getRegion();
        this.mbti = entity.getMbti();
        this.relationshipStatus = entity.getRelationshipStatus();
    }

}
