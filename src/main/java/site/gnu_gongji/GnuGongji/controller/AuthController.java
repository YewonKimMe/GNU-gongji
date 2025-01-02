package site.gnu_gongji.GnuGongji.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.gnu_gongji.GnuGongji.enums.ProcessCode;

import java.net.URI;

@Slf4j
@RestController
/**
 * 모바일 callback url, remoteConfig 에 등록되어 있으므로 url 순서 및 내용 수정 X
 * */
@RequestMapping("/api/v1/auth")
public class AuthController {

    /**
     * 모바일 callback url, remoteConfig 에 등록되어 있으므로 url 순서 및 내용 수정 X
     * */
    @GetMapping("/oauth2/logout/callback/kakao")
    public ResponseEntity<Void> kakaoLogoutCallback(@RequestParam("redirectUri") String redirectUri) {

        log.debug("redirectUri: {}", redirectUri);
        String newUri = redirectUri + "?process=" + ProcessCode.SUCCESS.getCode();
        // 딥링크 URL로 리다이렉트
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(newUri))
                .build();
    }
}
