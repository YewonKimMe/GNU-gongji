package site.gnu_gongji.GnuGongji.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import site.gnu_gongji.GnuGongji.dto.FcmMessageDto;
import site.gnu_gongji.GnuGongji.dto.FcmNotificationDto;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FcmService {

    @Value("${firebase.api-url}")
    private String API_URL;

    @Value("${firebase.json-directory}")
    private String jsonDir;

    private GoogleCredentials googleCredentials;

    @PostConstruct
    public void init() {
        String firebaseConfigPath = jsonDir;

        try (InputStream inputStream = new ClassPathResource(firebaseConfigPath).getInputStream()) {
            this.googleCredentials = GoogleCredentials
                    .fromStream(inputStream)
                    .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
            this.googleCredentials
                    .refreshIfExpired();
            // Firebase init
            FirebaseOptions firebaseOption = FirebaseOptions.builder()
                    .setCredentials(this.googleCredentials)
                    .build();
            FirebaseApp.initializeApp(firebaseOption);
            log.info("Firebase Application Initialized and configured;");
        } catch (IOException e) {
            log.error("[FCM INIT EXCEPTION] message={}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public int sendMessage(FcmNotificationDto fcmNotificationDto, boolean isValidateTest) {

        log.debug("[Execute sendMessage]");

        String message;

        try {
            message = createMessage(fcmNotificationDto, isValidateTest);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters()
                    .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + getAccessToken());

            HttpEntity<String> entity = new HttpEntity<>(message, headers);

            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);

            return response.getStatusCode() == HttpStatus.OK ? 1 : 0;

        } catch (HttpClientErrorException.NotFound e) {
            return 0;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // firebase 메소드 예외처리 내부에서 하기
    // 0 또는 갯수 리턴
    public int subscribeTopic(List<String> tokens, String topic) {
        try {
            TopicManagementResponse res = FirebaseMessaging.getInstance().subscribeToTopic(tokens, topic);
            int successCount = res.getSuccessCount();

            log.debug("[Firebase] FirebaseMessaging topic subscribe finished; topic={}; isAllFinished={}", topic, tokens.size() == successCount);

            return successCount;
        } catch (FirebaseMessagingException e) {

            log.debug("Firebase Topic Sub Failed, cause={}", e.getMessage());
            return 0;
        }
    }

    public int unSubscribeTopic(List<String> tokens, String topic) {
        int successCount = 0;
        try {
            TopicManagementResponse res = FirebaseMessaging.getInstance().unsubscribeFromTopic(tokens, topic);
            successCount = res.getSuccessCount();
            return successCount;
        } catch (FirebaseMessagingException e) {
            log.debug("Firebase Topic UnSub Failed, cause={}", e.getMessage());
            return successCount;
        }
    }

    public void sendMessageByTopic(String title, String body, String link, String notificationDate, String topic) {
        Message message = Message.builder()
                .setTopic(topic)
                .putData("title", title)
                .putData("body", body)
                .putData("link", link)
                .putData("notificationDate", notificationDate)
                .putData("uuid", UUID.randomUUID().toString())
                .build();
        try {
            String res = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent message to [" + topic + "] topic: " + res);
        } catch (FirebaseMessagingException e) {
            log.error("Firebase Topic Message Send Failed, cause={}", e.getMessage());
        }

    }

    private String getAccessToken() {

        try {
            googleCredentials.refreshIfExpired();
        } catch (IOException e) {
            log.error("[FCM getAccessToken IOException], cause={}", e.getMessage());
        }

        return googleCredentials.getAccessToken().getTokenValue();
    }

    private String createMessage(FcmNotificationDto fcmNotificationDto, boolean isValidateTest) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        FcmMessageDto fcmMessage = FcmMessageDto.builder()
                .message(
                        FcmMessageDto.Message.builder()
                                .token(fcmNotificationDto.getToken())
                                .notification(null)
                                .data(FcmMessageDto.Data.builder()
                                                .title(fcmNotificationDto.getTitle())
                                                .body(fcmNotificationDto.getBody())
                                        .link(fcmNotificationDto.getLink())
                                        .notificationDate(fcmNotificationDto.getNotificationDate())
                                        .uuid(fcmNotificationDto.getUuid())
                                                .image(null).build())
                                .build()
                ).validateOnly(isValidateTest).build();
        return objectMapper.writeValueAsString(fcmMessage);
    }
}
