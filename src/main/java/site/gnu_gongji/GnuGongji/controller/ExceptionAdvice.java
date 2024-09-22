package site.gnu_gongji.GnuGongji.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.gnu_gongji.GnuGongji.dto.response.FailResultAndMessage;
import site.gnu_gongji.GnuGongji.dto.response.ResultAndMessage;

@RestControllerAdvice
@Slf4j
public class ExceptionAdvice {

    // JWT Filter 예외 처리
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResultAndMessage> handleBadCredentialException(BadCredentialsException e) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new FailResultAndMessage(HttpStatus.UNAUTHORIZED.getReasonPhrase(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResultAndMessage> handleException(Exception e) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new FailResultAndMessage(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "unknown error"));
    }
}
