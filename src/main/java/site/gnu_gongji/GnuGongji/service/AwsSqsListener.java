package site.gnu_gongji.GnuGongji.service;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsSqsListener {

    @SqsListener(value = "${cloud.aws.sqs.queue.name}")
    public void listen(String message) {

        log.info("SQS poll message: {}", message);
    }
}
