package site.gnu_gongji.GnuGongji.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import site.gnu_gongji.GnuGongji.dto.FcmMessageDto;
import site.gnu_gongji.GnuGongji.dto.FcmNotificationDto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
public class FcmService {

    @Value("${firebase.api-url}")
    private String API_URL;

    public int sendMessage(FcmNotificationDto fcmNotificationDto) throws IOException {

        String message = createMessage(fcmNotificationDto);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + getAccessToken());

        HttpEntity<String> entity = new HttpEntity<>(message, headers);

        ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);


        return response.getStatusCode() == HttpStatus.OK ? 1 : 0;
    }

    private String getAccessToken() throws IOException {
        String firebaseConfigPath = "firebase/gnu-gongji-firebase-adminsdk-p9h0v-4fed761b99.json";

        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }

    private String createMessage(FcmNotificationDto fcmNotificationDto) throws JsonProcessingException {

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
                                                .image(null).build())
                                .build()
                ).validateOnly(false).build();
        return objectMapper.writeValueAsString(fcmMessage);
    }
}
