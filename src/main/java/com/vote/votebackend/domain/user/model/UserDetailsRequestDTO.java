package com.vote.votebackend.domain.user.model;

import com.vote.votebackend.domain.user.entity.enums.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDetailsRequestDTO {

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "닉네임은 한글, 영문, 숫자만 가능합니다.")
    private String nickname;

    @NotNull(message = "출생년도는 필수입니다.")
    @Min(value = 1900, message = "올바른 출생년도를 입력해주세요.")
    @Max(value = 2100, message = "미래에서 오셨나요?")
    private Integer birthYear;

    @NotNull(message = "성별은 필수입니다.")
    private Gender gender; // MALE, FEMALE

    @NotNull(message = "거주지는 필수입니다.")
    private Region region; // SEOUL, GYEONGGI ...

    @NotNull(message = "연애 상태는 필수입니다.")
    private RelationshipStatus relationshipStatus; // SINGLE, IN_RELATIONSHIP ...

    //선택 입력
    private MBTI mbti; // ISTJ ... (선택)

    private String introduce; // 한줄 소개 (선택)

    private String imageUrl; // 프로필 이미지 URL (선택)
}