package site.gnu_gongji.GnuGongji.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SendResult;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.gnu_gongji.GnuGongji.dto.ScrapNotification;

/**
 * Amazon SQS Service Component
 * */

@Slf4j
@RequiredArgsConstructor
@Service
public class AwsSqsSender implements NotificationSender {

    @Getter
    @Value("${cloud.aws.sqs.queue.name}")
    private String simpleServiceQueueName;

    private final SqsTemplate sqsTemplate;

    private final ObjectMapper objectMapper;

    @Override
    public boolean sendScrapNotification(ScrapNotification scrapNotification) {

        try {
            String message = objectMapper.writeValueAsString(scrapNotification);

            SendResult<Object> result = sqsTemplate.send(to -> to
                    .queue(simpleServiceQueueName)
                    .payload(message));

            log.debug("messageId: {}, message: {}", result.messageId(), result.message());
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
