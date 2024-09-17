package site.gnu_gongji.GnuGongji.security.oauth2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import site.gnu_gongji.GnuGongji.security.oauth2.enums.Unlink;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class KakaoOAuth2Unlinker implements OAuth2Unlinker {

    private final RestTemplate restTemplate;

    @Override
    public void unlink(String accessToken, OAuth2User oAuth2User) {

        OAuth2UserPrincipal oAuth2UserPrincipal = (OAuth2UserPrincipal) oAuth2User;

        Map<String, Object> map = new HashMap<>();

        log.info("[KAKAO] OAuth2 Unlink Requst, ACCESS_TOKEN={}", accessToken);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(accessToken);

        map.put("target_id_type", "user_id");
        map.put("target_id", Long.parseLong(oAuth2UserPrincipal.getUserInfo().getId()));
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, httpHeaders);

        restTemplate.exchange(Unlink.KAKAO.getUrl(), HttpMethod.POST, entity, String.class);
    }
}
