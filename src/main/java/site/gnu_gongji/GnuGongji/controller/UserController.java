package site.gnu_gongji.GnuGongji.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import site.gnu_gongji.GnuGongji.dto.EmailDto;
import site.gnu_gongji.GnuGongji.dto.UserSubDto;
import site.gnu_gongji.GnuGongji.dto.response.ResultAndMessage;
import site.gnu_gongji.GnuGongji.dto.response.SuccessResultAndMessage;
import site.gnu_gongji.GnuGongji.service.UserFeatureService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserFeatureService userFeatureService;

    // 유저 로그아웃 기능
    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication) {
        log.debug("auth={}", authentication.toString());
        return ResponseEntity.
                ok()
                .body("logout");
    }

    // 유저 탈퇴
    @DeleteMapping("/withdrawal")
    public ResponseEntity<?> withdrawal(Authentication authentication) {
        return null;
    }

    // 유저 이메일 변경
    @PatchMapping("/email")
    public ResponseEntity<ResultAndMessage> changeUserEmail(@Validated @RequestBody EmailDto emailDto,
                                             Authentication authentication) {
        userFeatureService.updateUserEmail(authentication.getName(), emailDto);
        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), emailDto.getEmail() + " 으로 이메일이 변경되었습니다."));
    }

    // 유저 구독 등록
    @PostMapping("/notice-subscription")
    public ResponseEntity<ResultAndMessage> enrollSubscription(@RequestBody UserSubDto userSubDto,
                                                                       Authentication authentication) {

        userFeatureService.addUserSubDepartment(authentication.getName(), userSubDto.getDepartmentId());

        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), "공지사항 구독이 추가되었습니다."));
    }

    // 유저 구독 삭제
    @DeleteMapping("/notice-subscription")
    public ResponseEntity<ResultAndMessage> deleteSubscription(@Validated @RequestBody UserSubDto userSubDto,
                                                Authentication authentication) {
        userFeatureService.deleteSubscription(authentication.getName(), userSubDto.getDepartmentId());

        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), "공지사항 구독이 삭제되었습니다."));
    }
}
