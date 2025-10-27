package com.vote.votebackend.domain.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.swing.*;

@Getter
@Setter
@ToString
public class UserRequestDTO {

    public interface existGroup {     //회원가입시 username 존재 확인
    }

    public interface addGroup {     //회원 가입 시
    }

    public interface passwordGroup { // 비밀번호 변경시
    }

    public interface deleteGroup { // 회원 탈퇴 시
    }

    public interface updateGroup { // 회원 수정 시
    }


    @NotBlank(groups = {existGroup.class, addGroup.class,updateGroup.class, deleteGroup.class}) @Size(min=4)
    private String username;
    @NotBlank(groups = {addGroup.class, passwordGroup.class}) @Size(min=4)
    private String password;
    @NotBlank(groups = { addGroup.class,updateGroup.class})
    private String nickname;
    @Email (groups = {addGroup.class,updateGroup.class})
    private String email;


}


