package site.gnu_gongji.GnuGongji.security.oauth2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Component
public class KakaoOAuth2Unlinker implements OAuth2Unlinker {

    private final RestTemplate restTemplate;

    @Override
    public void unlink(String accessToken) {

        log.info("[KAKAO] OAuth2 Unlink Requst, ACCESS_TOKEN={}", accessToken);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>("", httpHeaders);

        restTemplate.exchange(Unlink.KAKAO.getUrl(), HttpMethod.POST, entity, String.class);
    }
}
