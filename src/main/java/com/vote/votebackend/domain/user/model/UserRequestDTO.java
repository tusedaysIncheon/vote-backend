package com.vote.votebackend.domain.user.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserRequestDTO {

    private String username;

    public CharSequence getPassword() {
    }

    public String getNickname() {
    }

    public String getEmail() {
    }


