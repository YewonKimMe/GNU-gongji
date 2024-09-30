package site.gnu_gongji.GnuGongji.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.gnu_gongji.GnuGongji.dto.FcmTokenDto;
import site.gnu_gongji.GnuGongji.dto.response.ResultAndMessage;
import site.gnu_gongji.GnuGongji.dto.response.SuccessResultAndMessage;
import site.gnu_gongji.GnuGongji.service.UserManageService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/firebase")
public class FcmController {

    private final UserManageService userManageService;

    @PostMapping("/token")
    public ResponseEntity<ResultAndMessage> getUserIdentifyToken(@RequestBody FcmTokenDto fcmTokenDto, Authentication authentication) {

        log.debug("[token 등록 요청] userId={}", authentication.getName());

        userManageService.updateUserToken(authentication.getName(), fcmTokenDto.getToken());

        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), authentication.getName() + " 계정의 알림 설정 등록이 완료되었습니다."));
    }

}
