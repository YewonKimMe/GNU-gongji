package site.gnu_gongji.GnuGongji.service;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.Attachment;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.webhook.Payload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.gnu_gongji.GnuGongji.dto.ScrapResult;
import site.gnu_gongji.GnuGongji.dto.ScrapResultDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;

@Slf4j
@Service
public class SlackService {

    @Value("${slack.webhook.url}")
    private String slackWebhookUrl;

    @Value("${slack.bot-token}")
    private String slackBotToken;

    @Value("${slack.channel-id}")
    private String slackChannelId;

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
                .text("@channel " + "GNU 공지 BOT: 작업 결과 보고")
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

        String titleText = "GNU 공지 BOT: 작업 결과 보고\n\n";

        StringBuilder successSb = new StringBuilder();
        for (ScrapResultDto scrapResultDto : successResults) {
            String departmentName = scrapResultDto.getDepartmentName();
            int successSize = scrapResultDto.getScrapResultList().size();
            successSb.append(String.format("[%s] 스크랩 결과: %d\n", departmentName, successSize));
            List<ScrapResult> scrapResultList = scrapResultDto.getScrapResultList();

            for (ScrapResult scrapResult : scrapResultList) {
                successSb.append(scrapResult.getNoticeLink()).append("\n");
            }
        }

        StringBuilder failSb = new StringBuilder();
        for (ScrapService.ScrapFailedDto failResult : failResults) {
            String departmentKo = failResult.getDepartmentKo();
            String reason = failResult.getReason();
            String formattedNoticeLinkUrl = failResult.getFormattedNoticeLinkUrl();
            failSb.append(String.format("[%s] 스크랩 실패, 사유: [%s], 링크: %s\n", departmentKo, reason, formattedNoticeLinkUrl));
        }

        if (successResults.isEmpty()) {
            successSb.append("신규 등록된 공지사항이 없습니다.");
        }

        if (failResults.isEmpty()) {
            failSb.append("스크랩 과정 중 발생한 오류가 없습니다.");
        }

        // 현재 시간 가져오기
        LocalDateTime now = LocalDateTime.now();
        String currentTime = now.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분"));

        // Block Kit을 사용하여 메시지 구성
        List<LayoutBlock> blocks = Arrays.asList(
                header(header -> header.text(plainText("GNU 공지 BOT: " + currentTime))),
                // 멘션을 위한 섹션
                section(section -> section.text(
                        markdownText("<!channel> 스크랩 작업 결과") // 멘션 포함
                )),
                divider(),
                section(section -> section.text(markdownText("*성공 결과*\n```" + successSb.toString() + "```"))),
                divider(),
                section(section -> section.text(markdownText("*실패 결과*\n```" + failSb.toString() + "```"))),
                divider()
        );

        try {
            MethodsClient methods = slack.methods(slackBotToken);
            ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                    .channel(slackChannelId)  // 채널 ID를 지정
                    .text("<@channel> "+ titleText)
                    .blocks(blocks)
                    .mrkdwn(true)
                    .parse("full")  // <!channel>과 같은 특수 멘션이 파싱
                    .build();

            ChatPostMessageResponse response = methods.chatPostMessage(request);

            if (!response.isOk()) {
                log.error("[RESPONSE NOT OK] SLACK 메세지 전송 실패: {}", response.getError());
            }
        } catch (Exception e) {
            log.error("[Exception] SLACK 메세지 전송 실패: {}", e.getMessage());
        }
    }

}
