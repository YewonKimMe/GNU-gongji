package site.gnu_gongji.GnuGongji.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import site.gnu_gongji.GnuGongji.dto.*;
import site.gnu_gongji.GnuGongji.dto.response.ResultAndMessage;
import site.gnu_gongji.GnuGongji.dto.response.SuccessResultAndMessage;
import site.gnu_gongji.GnuGongji.service.UserFeatureService;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserFeatureService userFeatureService;

    @GetMapping("/user-information")
    public ResponseEntity<UserInfoDto> getUserInfo(Authentication authentication) {
        UserInfoDto userInfo = userFeatureService.getUserInfoByOAuthId(authentication.getName());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(10, TimeUnit.SECONDS))
                .body(userInfo);
    }

    @GetMapping("/token-status")
    public ResponseEntity<UserTokenStatusDto> getUserTokenValidStatus(Authentication authentication) {
        boolean isTokenValid = userFeatureService.checkUserFCMToken(authentication.getName());

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.SECONDS))
                .body(new UserTokenStatusDto(isTokenValid));
    }

    @GetMapping("/devices")
    public ResponseEntity<ResultAndMessage> getAllUserDeviceAndStatus(Authentication authentication) {

        List<UserTokenStatus> userDevicesAndStatus = userFeatureService.getUserDevicesAndStatus(authentication.getName());

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.SECONDS))
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), userDevicesAndStatus));
    }

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
                                                               Authentication authentication, HttpServletRequest request) {

        int successSubCnt = userFeatureService.addUserSubDepartment(authentication.getName(), userSubDto.getDepartmentId());

        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), "공지사항 구독이 등록된 " + successSubCnt + " 개의 기기에 추가되었습니다."));
    }

    // 유저 구독 삭제
    @DeleteMapping("/notice-subscription")
    public ResponseEntity<ResultAndMessage> deleteSubscription(@RequestParam(name = "departmentId") Long departmentId,
                                                Authentication authentication) {
        int successUnSubCnt = userFeatureService.deleteSubscription(authentication.getName(), departmentId);

        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), "공지사항 구독이 " + successUnSubCnt + " 개의 기기에서 삭제되었습니다."));
    }

    // 내 구독 정보 조회
    @GetMapping("/my-notice-subscription")
    public ResponseEntity<ResultAndMessage> getUserSub(Authentication authentication) {

        log.debug("[USER SUB] userId={}", authentication.getName());

        List<DepartmentDto> userSub = userFeatureService.getUserSub(authentication.getName());

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(3, TimeUnit.SECONDS))
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), userSub));
    }

    // 유저 공지 저장(+ 메모)
    @PostMapping("/memo-notifications")
    public ResponseEntity<ResultAndMessage> saveNotification(@RequestBody UserMemoNotificationDto userMemoNotificationDto, Authentication authentication) {
        userFeatureService.saveUserMemoNotification(userMemoNotificationDto, authentication);
        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), "저장되었습니다."));
    }

    // 유저 공지 삭제
    @DeleteMapping("/memo-notifications/{id}")
    public ResponseEntity<ResultAndMessage> deleteNotification(Authentication authentication, @PathVariable(name = "id") Long id) {

        userFeatureService.deleteUserMemoNotification(id, authentication);
        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), "동기화된 공지사항이 삭제되었습니다."));
    }

    // 유저 공지 모두 삭제
    @DeleteMapping("/memo-notifications")
    public ResponseEntity<ResultAndMessage> deleteAllNotification(Authentication authentication) {

        int cnt = userFeatureService.deleteAllUserMemoNotifications(authentication.getName());

        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), "동기화된 공지사항 " + cnt + "개가 모두 삭제되었습니다."));
    }

    // 유저 공지 메모 수정
    @PatchMapping("/memo-notifications/{id}")
    public ResponseEntity<ResultAndMessage> updateNotification(Authentication authentication, @PathVariable(name = "id") Long id, @RequestBody UserMemoNotificationDto userMemoNotificationDto) {

        userFeatureService.updateUserMemoNotification(id, userMemoNotificationDto, authentication);

        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), "메모가 수정되었습니다."));
    }

    // 유저의 모든 메모
    @GetMapping("/memo-notifications")
    public ResponseEntity<ResultAndMessage> getUserMemoNotifications(Authentication authentication) {
        log.debug("GET /memo-notifications");
        List<UserMemoNotificationDto> userMemoNotifications = userFeatureService.getUserMemoNotifications(authentication.getName());

        log.debug("GET /memo-notifications, userMemoNotifications={}", userMemoNotifications);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(2, TimeUnit.SECONDS))
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), userMemoNotifications));
    }

}
