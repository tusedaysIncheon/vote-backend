package com.vote.votebackend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {

    // --- Validation Groups (검증 그룹 정의) ---
    public interface existGroup {}  // 중복 확인용
    public interface addGroup {}    // 회원가입용
    public interface updateGroup {} // 정보 수정용
    public interface deleteGroup {} // 회원 탈퇴용

    // --- 필드 정의 ---

    @Schema(description = "사용자 아이디", example = "voteuser123")
    @NotBlank(message = "아이디는 필수입니다.", groups = {addGroup.class, existGroup.class, deleteGroup.class})
    @Size(min = 5, max = 20, message = "아이디는 5자 이상 20자 이하로 작성해주세요.", groups = {addGroup.class, existGroup.class})
    // Zod: .string().min(5).max(20)
    private String username;

    @Schema(description = "비밀번호", example = "password123!")
    @NotBlank(message = "비밀번호는 필수입니다.", groups = {addGroup.class, deleteGroup.class})
    @Size(min = 8, message = "비밀번호는 8자 이상 입력해주세요.", groups = {addGroup.class})
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>])(?!.*\\s).{8,}$",
            message = "비밀번호는 소문자, 숫자, 특수문자를 포함해야 하며 공백을 포함할 수 없습니다.",
            groups = {addGroup.class}
    )
    private String password;

    @Schema(description = "이메일", example = "vote@gmail.com")
    @NotBlank(message = "이메일은 필수입니다.", groups = {addGroup.class, updateGroup.class})
    @Email(message = "유효한 이메일 주소를 입력해주세요.", groups = {addGroup.class, updateGroup.class})
    private String email;


}