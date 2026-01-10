package com.vote.votebackend.global.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.rmi.AccessException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    //400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(
            IllegalArgumentException e,
            HttpServletRequest request
            ){

        log.warn("접근 권한 오류 발생 요청 IP: {}, URL: {}", request.getRemoteAddr(), request.getRequestURI());

        Map<String,String> errorResponse = new HashMap<>();
        errorResponse.put("code","BAD_REQUEST");
        errorResponse.put("message",e.getMessage());


        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    //403
    @ExceptionHandler(AccessException.class)
    public ResponseEntity<Map<String, String>> handleAccessException(
            AccessException e
    ){
        log.warn(e.getMessage());
        Map<String,String> errorResponse = new HashMap<>();
        errorResponse.put("code","FORBIDDEN");
        errorResponse.put("message","접근 권한이 없습니다.");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    //500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(
            Exception e,
            HttpServletRequest request
    ){
        log.error("서버 내부 치명적 오류! URL: {}", request.getRequestURI(), e);

        Map<String,String> errorResponse = new HashMap<>();
        errorResponse.put("code","INTERNAL_SERVER_ERROR");
        errorResponse.put("message","서버 오류");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExeption(MethodArgumentNotValidException e){
        String errorMessage = "잘못된 요청입니다.";
        if(e.getBindingResult().hasErrors()) {
            String specificMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();

            if(specificMessage != null && !specificMessage.isBlank()){
                errorMessage = specificMessage;
            }
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", errorMessage));
    }


}
