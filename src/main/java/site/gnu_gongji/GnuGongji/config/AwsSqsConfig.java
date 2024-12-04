package site.gnu_gongji.GnuGongji.config;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class AwsSqsConfig {

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKeyId;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretAccessKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    /**
     * SQS Async Client Bean
     * */

    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        return SqsAsyncClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .region(Region.of(region))
                .build();
    }

    /**
     * SQS Message Create Template
     * */

    @Bean
    public SqsTemplate sqsTemplate() {
        return SqsTemplate.newTemplate(sqsAsyncClient());
    }

    @Bean
    public SqsMessageListenerContainerFactory<Object> sqsMessageListenerContainerFactory() {
        return SqsMessageListenerContainerFactory.builder()
                .configure(sqsContainerOptionsBuilder ->
                        sqsContainerOptionsBuilder
                                .maxConcurrentMessages(10) // 컨테이너 풀 사이즈
                                .maxMessagesPerPoll(10) // 한번 폴링 당 최대 수신 가능한 메세지 사이즈
                                .acknowledgementInterval(Duration.ofSeconds(5)) // AWS SQS 응답 간격
                                .acknowledgementThreshold(10) // AWS SQS 응답 갯수 간격
                ).sqsAsyncClient(sqsAsyncClient())
                .build();
    }
}
