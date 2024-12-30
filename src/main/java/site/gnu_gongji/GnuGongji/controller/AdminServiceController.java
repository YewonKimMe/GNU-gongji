package site.gnu_gongji.GnuGongji.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.gnu_gongji.GnuGongji.dto.FcmNotificationDto;
import site.gnu_gongji.GnuGongji.dto.ScrapNotification;
import site.gnu_gongji.GnuGongji.dto.response.ResultAndMessage;
import site.gnu_gongji.GnuGongji.dto.response.SuccessResultAndMessage;
import site.gnu_gongji.GnuGongji.entity.Department;
import site.gnu_gongji.GnuGongji.enums.Topic;
import site.gnu_gongji.GnuGongji.repository.AdminRepository;
import site.gnu_gongji.GnuGongji.service.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@RestController
@Tag(name = "Admin", description = "관리자 기능 관련 엔드포인트")
@RequestMapping("/api/v1/admin")
public class AdminServiceController {

    @Value("${mycustom.sub-limit}")
    private int subLimit;

    private final AdminRepository adminRepository;

    private final NoticeExcelParser noticeExcelParser;

    private final DepartmentService departmentService;

    private final FcmService fcmService;

    private final SlackService slackService;

    private final AwsSqsSender awsSqsSender;

    @Operation(summary = "부서 엑셀 파일 등록", description = "부서 엑셀 파일을 등록하는 API")
    @PostMapping(value = "/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResultAndMessage> insertNoticeExcel(@Parameter(
            description = "학과 엑셀 파일, 양식 확인 필수",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
    )@RequestPart("multipartFile") MultipartFile file) {

        noticeExcelParser.readDepartmentInfoExcel(file);
        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), "업로드 완료"));
    }

    @GetMapping("/all-dept")
    public ResponseEntity<List<Department>> getAllDpt() {
        List<Department> allDepartmentNoticeInfo = departmentService.getAllDepartmentNoticeInfo();

        return ResponseEntity.ok()
                .body(allDepartmentNoticeInfo);
    }

    @PostMapping("/firebase-notification-test")
    public ResponseEntity<ResultAndMessage> sendNotification(@RequestBody FcmNotificationDto fcmNotificationDto) {


        fcmNotificationDto.setUuid(UUID.randomUUID().toString());
        log.debug("fcmNotificationDto: {}", fcmNotificationDto);
        fcmService.sendMessage(fcmNotificationDto, false);

        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), "전송완료"));

    }

    @PostMapping("/firebase-topic-subscribe-test")
    public ResponseEntity<ResultAndMessage> addSubscribeTopic(@RequestBody TokenDto tokenDto) {

        String topic = Topic.ONLY_TEST_TOPIC_PATH.getPath();

        StringBuilder sb = new StringBuilder();

        sb.append(topic)
                .append("으로 [")
                .append(tokenDto.getToken())
                .append("] 토큰이 ");

        if (tokenDto.isOption()) {
            fcmService.subscribeTopic(List.of(tokenDto.getToken()), topic);
            sb.append("구독 등록");
        } else {
            fcmService.unSubscribeTopic(List.of(tokenDto.getToken()), topic);
            sb.append("구독 해제");
        }
        sb.append("됨");

        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), sb.toString()));
    }

    @PostMapping("/firebase-topic-messaging-test")
    public ResponseEntity<ResultAndMessage> sendTopicMessaging() {

        fcmService.sendMessageByTopic("[테스트] 경상국립대 장학공지", "[테스트] ★★ 2025학년도 1학기 국가장학금 1차 신청 안내 ★★ (2024.11.15)", "https://www.gnu.ac.kr/main/na/ntt/selectNttInfo.do?mi=1376&bbsId=1075&nttSn=2259007",
                "2024.11.15", Topic.ONLY_TEST_TOPIC_PATH.getPath(), UUID.randomUUID().toString());

        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), Topic.ONLY_TEST_TOPIC_PATH + " 토픽으로 전송 완료"));
    }

    @PostMapping("/update-sub-limit-count")
    public ResponseEntity<ResultAndMessage> updateSubLimitCount(@RequestBody SubLimitDto subLimitDto) {

        adminRepository.updateSubscribeLimit(subLimitDto.getSubLimit());

        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), "모든 유저의 구독 제한 갯수를 " + subLimitDto.getSubLimit() + " 으로 업데이트 완료"));
    }

    @PostMapping("/slack-message-send")
    public ResponseEntity<ResultAndMessage> sendSlackMessage(@RequestBody SlackMessageDto messageDto) {

        slackService.sendSimpleTextMessage(messageDto.text, messageDto.text, true);

        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), "슬랙 채널로 메세지 발송 완료"));
    }

    @PostMapping("/sqs-test")
    public ResponseEntity<ResultAndMessage> sendToSqs(@RequestBody ScrapNotification scrapNotification) {

        String uuid = UUID.randomUUID().toString();

        scrapNotification.setMessageId(uuid);
        scrapNotification.getScrapResult().setUuid(uuid);
        awsSqsSender.sendScrapNotification(scrapNotification);

        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), "SQS 메세지 발송 완료"));
    }

    @Getter
    @Setter
    private static class SlackMessageDto {
        private String text;
    }

    @Getter
    @Setter
    public static class TokenDto {
        private String token;
        private boolean option;
    }

    @Getter
    @Setter
    public static class SubLimitDto {
        private Integer subLimit;
    }
}
