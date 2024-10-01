package site.gnu_gongji.GnuGongji.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import site.gnu_gongji.GnuGongji.dto.FcmNotificationDto;
import site.gnu_gongji.GnuGongji.dto.response.ResultAndMessage;
import site.gnu_gongji.GnuGongji.dto.response.SuccessResultAndMessage;
import site.gnu_gongji.GnuGongji.entity.User;
import site.gnu_gongji.GnuGongji.service.DepartmentService;
import site.gnu_gongji.GnuGongji.service.FcmService;
import site.gnu_gongji.GnuGongji.service.ScrapService;
import site.gnu_gongji.GnuGongji.service.UserManageService;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Test", description = "테스트 컨트롤러")
@RequestMapping("/api/v1/test")
public class TestController {

    private final ScrapService scrapService;

    private final DepartmentService departmentService;

    private final UserManageService userManageService;

    private final FcmService fcmService;

    @GetMapping
    public ResponseEntity<?> test(OAuth2AuthenticationToken token) {
//        log.info("token={}", token.getPrincipal());
//        log.info("token={}", token.getCredentials());
//        log.info("token={}", token.getName());
        return ResponseEntity.ok("hello");
    }

    @GetMapping("/oidc-principal")
    public OidcUser getOidcUserPrincipal(
            @AuthenticationPrincipal OidcUser principal) {
        return principal;
    }

    @GetMapping("/url-test")
    public ResponseEntity<String> urlTest() {
        //scrapService.scrap();
        return ResponseEntity.ok()
                .body("OK");
    }

    @GetMapping("/user-find-test")
    public ResponseEntity<ResultAndMessage> getUsersWithCondition() {
        List<User> result = userManageService.findUsersWithActiveSubscriptionsAndNotifications();

        log.info("[Result] {}", result.toString());
        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), "OK"));
    }

    @PostMapping("/noti-test")
    public ResponseEntity<ResultAndMessage> sendNotification(@RequestBody FcmNotificationDto fcmNotificationDto) {
        try {
            int i = fcmService.sendMessage(fcmNotificationDto);
            log.debug("[send result={}]", i);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), "OK"));
    }
}
