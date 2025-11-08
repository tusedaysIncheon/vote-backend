package com.vote.votebackend.domain.user.model;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class LoginRequestDTO {

    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String deviceId;
}
