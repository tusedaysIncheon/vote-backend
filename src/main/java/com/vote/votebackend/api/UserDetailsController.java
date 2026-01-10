package com.vote.votebackend.api;


import com.vote.votebackend.domain.user.model.UserDetailsRequestDTO;
import com.vote.votebackend.domain.user.model.UserDetailsResponseDTO;
import com.vote.votebackend.domain.user.service.UserDetailService;
import com.vote.votebackend.global.security.custom.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/user-details")
@RequiredArgsConstructor
public class UserDetailsController {

    private final UserDetailService userDetailService;

    @PostMapping
    public ResponseEntity<String> saveUserDetails(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody UserDetailsRequestDTO dto
            ){

        String username = user.getUsername();
        log.info("save user details : {}", username);

        userDetailService.saveUserDetails(username, dto);

        return ResponseEntity.ok("프로필이 성공적으로 저장되었습니다.");
    }

    @GetMapping
    public ResponseEntity<UserDetailsResponseDTO> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails user){

        String username = user.getUsername();
        log.info("get user details : {}", username);

        UserDetailsResponseDTO profile =  userDetailService.getProfile(username);
        return  ResponseEntity.ok(profile);
    }

    @GetMapping("/exist-nickname")
    public ResponseEntity<Boolean> existNickname(@RequestParam String nickname){
        log.info("exist nickname : {}", nickname);
        return ResponseEntity.ok(userDetailService.existNickname(nickname));

    }

}
