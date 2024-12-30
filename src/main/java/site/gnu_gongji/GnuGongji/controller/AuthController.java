package site.gnu_gongji.GnuGongji.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @GetMapping("/oauth2/logout/callback/kakao")
    public ResponseEntity<Void> kakaoLogoutCallback(@RequestParam("redirectUri") String redirectUri) {

        log.debug("redirectUri: {}", redirectUri);

        // 딥링크 URL로 리다이렉트
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(redirectUri))
                .build();
    }
}
