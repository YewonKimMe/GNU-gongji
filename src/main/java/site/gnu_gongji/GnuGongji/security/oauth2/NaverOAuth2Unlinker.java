package site.gnu_gongji.GnuGongji.security.oauth2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import site.gnu_gongji.GnuGongji.exception.OAuth2UnlinkException;

@Slf4j
@RequiredArgsConstructor
@Component
public class NaverOAuth2Unlinker implements OAuth2Unlinker {

    private static final String URL = Unlink.NAVER.getUrl();

    private final RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String clientSecret;

    @Override
    public void unlink(String accessToken, OAuth2User oAuth2User) {

        log.info("[NAVER] OAuth2 Unlink Requst, ACCESS_TOKEN={}", accessToken);

        String url = URL +
                "?service_provider=NAVER" +
                "&grant_type=delete" +
                "&client_id=" +
                clientId +
                "&client_secret=" +
                clientSecret +
                "&access_token=" +
                accessToken;

        UnlinkResponse response = restTemplate.getForObject(url, UnlinkResponse.class);

        if (response != null && !"success".equalsIgnoreCase(response.getResult())) {
            throw new OAuth2UnlinkException("Failed to unlink NAVER OAuth2");
        }
    }

    @Getter
    @RequiredArgsConstructor
    private static class UnlinkResponse {

        @JsonProperty("access_token")
        private final String accessToken;

        private final String result;
    }
}
