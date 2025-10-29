package com.vote.votebackend.domain.jwt.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class RefreshRequestDTO {

        @NotBlank
        private String refreshToken;
}
