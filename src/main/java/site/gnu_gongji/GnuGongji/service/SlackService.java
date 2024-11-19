package site.gnu_gongji.GnuGongji.service;

import com.slack.api.Slack;
import com.slack.api.model.Attachment;
import com.slack.api.webhook.Payload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.gnu_gongji.GnuGongji.dto.ScrapResultDto;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class SlackService {

    @Value("${slack.webhook.url}")
    private String slackWebhookUrl;

    private final Slack slack = Slack.getInstance();

    public void sendSimpleTextMessage(String text) {

        //Payload payload = Payload.builder().text(text).build();
        // 녹색 attachment 생성
        Attachment greenAttachment = Attachment.builder()
                .color("good")  // "good"은 녹색을 나타냅니다
                .title("성공 메시지")
                .text("작업이 성공적으로 완료되었습니다.\n작업이 성공적으로 완료되었습니다.\n작업이 성공적으로 완료되었습니다.\n")
                .fallback("작업 성공")
                .build();

        // 빨간색 attachment 생성
        Attachment redAttachment = Attachment.builder()
                .color("danger")  // "danger"는 빨간색을 나타냅니다
                .title("실패 메시지")
                .text("작업 중 오류가 발생했습니다.")
                .fallback("작업 실패")
                .build();

        // Payload 생성 및 attachment 추가
        Payload payload = Payload.builder()
                .text("작업 결과 보고")
                .attachments(Arrays.asList(greenAttachment, redAttachment))
                .build();
        try {
            slack.send(slackWebhookUrl, payload);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // SCRAP RESULT_SEND TO SLACK
    // 스크랩된 공지사항이 있거나, 스크랩이 실패한 공지사항이 있는 경우에만 전송
    public void sendScrapResultMessage(List<ScrapResultDto> successResults, List<ScrapService.ScrapFailedDto> failResults) {

        String titleText = "공지사항 스크랩 성공/실패 결과 메세지\n\n";

        StringBuilder sb = new StringBuilder();

        for (ScrapResultDto scrapResultDto : successResults) {
            String departmentName = scrapResultDto.getDepartmentName();
            int successSize = scrapResultDto.getScrapResultList().size();
            sb.append(String.format("[%s] 스크랩 결과: %d\n", departmentName, successSize));
        }

        if (successResults.isEmpty()) {
            sb.append("수집된 공지사항이 없습니다.");
        }
        // 녹색 attachment 생성
        Attachment greenAttachment = Attachment.builder()
                .color("good")  // "good"은 녹색
                .title("공지사항 스크랩 성공 작업 결과")
                .text(sb.toString())
                .fallback("작업 성공")
                .build();

        sb.setLength(0); // StringBuilder 초기화

        for (ScrapService.ScrapFailedDto failResult : failResults) {
            String departmentKo = failResult.getDepartmentKo();
            String reason = failResult.getReason();
            String formattedNoticeLinkUrl = failResult.getFormattedNoticeLinkUrl();
            sb.append(String.format("[%s] 스크랩 실패, 사유: [%s], 링크: %s\n", departmentKo, reason, formattedNoticeLinkUrl));
        }

        if (failResults.isEmpty()) {
            sb.append("오류 결과가 없습니다.");
        }

        // 빨간색 attachment 생성
        Attachment redAttachment = Attachment.builder()
                .color("danger")  // "danger"는 빨간색을 나타냅니다
                .title("공지사항 스크랩 실패 작업 결과")
                .text(sb.toString())
                .fallback("작업 실패")
                .build();

        Payload payload = Payload.builder()
                .text(titleText)
                .attachments(Arrays.asList(greenAttachment, redAttachment))
                .build();

        try {
            slack.send(slackWebhookUrl, payload);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
