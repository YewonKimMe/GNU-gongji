package site.gnu_gongji.GnuGongji.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.gnu_gongji.GnuGongji.service.ScrapService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/aws/sqs")
public class AwsSqsSyncController {

    private final ScrapService scrapService;

    @Value("${mycustom.sqs-update-secret}")
    private String secret;

    @PostMapping
    public ResponseEntity<String> getAlertServerSync(@RequestBody SyncMessageDto syncMessageDto) {

        if (!syncMessageDto.getAuthToken().equals(secret)) throw new RuntimeException("올바르지 않은 토큰입니다.");
        scrapService.setSqsStatus(syncMessageDto.status);

        return ResponseEntity.ok("OK");
    }

    @Getter
    @Setter
    public static class SyncMessageDto {
        String authToken;
        boolean status;
    }
}
