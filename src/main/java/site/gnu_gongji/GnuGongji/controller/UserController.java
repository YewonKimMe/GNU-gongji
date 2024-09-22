package site.gnu_gongji.GnuGongji.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    // TEST
    @GetMapping
    public ResponseEntity<String> test() {
        return ResponseEntity.ok()
                .body("TEST");
    }

    // 유저 로그아웃 기능
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
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
    public ResponseEntity<?> changeUserEmail(Authentication authentication) {
        return null;
    }

    // 유저 구독 등록
    @PostMapping("/notice-subscription")
    public ResponseEntity<?> enrollSubscription(Authentication authentication) {
        return null;
    }

    // 유저 구독 삭제
    @DeleteMapping("/notice-subscription")
    public ResponseEntity<?> deleteSubscription(Authentication authentication) {
        return null;
    }
}
