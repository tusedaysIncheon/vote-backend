package com.vote.votebackend.global.util;

import com.vote.votebackend.domain.user.entity.UserEntity;
import com.vote.votebackend.global.security.custom.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static UserEntity getCurrentUser(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if( authentication == null || authentication.getPrincipal() instanceof String){
            return null;
        }

        try{
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            return customUserDetails.getUserEntity();
        } catch(ClassCastException e) {
            return null;
        }

    }

    public static Long getCurrentUserId(){
        UserEntity userEntity = getCurrentUser();
        return (userEntity == null) ? null : userEntity.getId();
    }

}
