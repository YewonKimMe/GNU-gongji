package site.gnu_gongji.GnuGongji.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import site.gnu_gongji.GnuGongji.dto.FcmTokenDto;
import site.gnu_gongji.GnuGongji.dto.response.ResultAndMessage;
import site.gnu_gongji.GnuGongji.dto.response.SuccessResultAndMessage;
import site.gnu_gongji.GnuGongji.enums.Device;
import site.gnu_gongji.GnuGongji.service.UserManageService;
import site.gnu_gongji.GnuGongji.tool.DeviceUtil;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/firebase")
public class FcmController {

    private final UserManageService userManageService;

    @PostMapping("/token")
    public ResponseEntity<ResultAndMessage> enrollFcmToken(@RequestBody FcmTokenDto fcmTokenDto, Authentication authentication, HttpServletRequest request) {

        log.debug("[token 등록 요청] userId={}", authentication.getName());
        log.info("[token 등록 요청] userId={}", authentication.getName());

        Device deviceInfo = DeviceUtil.getDeviceInfo(request);

        userManageService.updateUserFcmToken(authentication.getName(), fcmTokenDto.getToken(), deviceInfo);

        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), authentication.getName() + " 계정의 알림 설정 등록이 완료되었습니다."));
    }

    @DeleteMapping("/token")
    public ResponseEntity<ResultAndMessage> deleteFcmToken(@RequestParam(name = "option") String option, @RequestParam(name = "platform", required = false) String platform,  Authentication authentication, HttpServletRequest request) {

        log.debug("[token 삭제 요청] userId={}", authentication.getName());
        log.info("[token 삭제 요청] userId={}", authentication.getName());

        userManageService.deleteUserFcmToken(authentication.getName(), option, platform);

        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), authentication.getName() + " 계정의 알림 설정이 해지되었습니다."));
    }

}
