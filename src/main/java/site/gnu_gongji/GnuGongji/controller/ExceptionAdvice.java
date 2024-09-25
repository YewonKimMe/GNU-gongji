package site.gnu_gongji.GnuGongji.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.gnu_gongji.GnuGongji.dto.response.FailResultAndMessage;
import site.gnu_gongji.GnuGongji.dto.response.ResultAndMessage;
import site.gnu_gongji.GnuGongji.exception.*;


@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    // JWT Filter 예외 처리
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResultAndMessage<String>> handleBadCredentialException(BadCredentialsException e) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new FailResultAndMessage<>(HttpStatus.UNAUTHORIZED.getReasonPhrase(), e.getMessage()));
    }

    // 사용자 정의 예외 핸들러
    @ExceptionHandler({
            DepartmentNotExistException.class,
            DuplicateSubscribeException.class,
            NotExistKeyException.class,
            SubscriptionLimitReachedException.class,
            UserNotExistException.class
    })
    public ResponseEntity<ResultAndMessage<String>> handleMultiException(Exception e) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new FailResultAndMessage<>(HttpStatus.BAD_REQUEST.getReasonPhrase(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResultAndMessage<String>> handleException(Exception e) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new FailResultAndMessage<>(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "서버에 오류가 발생했습니다."));
    }
}
