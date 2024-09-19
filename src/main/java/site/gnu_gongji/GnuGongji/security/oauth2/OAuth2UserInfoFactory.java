package site.gnu_gongji.GnuGongji.security.oauth2;

import lombok.extern.slf4j.Slf4j;
import site.gnu_gongji.GnuGongji.exception.OAuth2AuthenticationProcessingException;
import site.gnu_gongji.GnuGongji.security.oauth2.enums.OAuth2Provider;

import java.util.Map;

@Slf4j
public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAtu2UserInfo(String registrationId, String accessToken, Map<String, Object> attributes) {

        log.debug("attributes={}", attributes);

        if (OAuth2Provider.GITHUB.getRegistrationId().equals(registrationId)) {
            // GitHub OAuth2 UserInfo
            return new GithubOAuth2UserInfo(accessToken, attributes);
        } else if (OAuth2Provider.KAKAO.getRegistrationId().equals(registrationId)) {
            // Kakao OAuth2 UserInfo
            return new KakaoOAuth2UserInfo(accessToken, attributes);
        } else if (OAuth2Provider.NAVER.getRegistrationId().equals(registrationId)) {
            // Naver OAuth2 UserInfo
            return new NaverOAuth2UserInfo(accessToken, attributes);
        } else throw new OAuth2AuthenticationProcessingException(registrationId + " Login is not support.");
    }
}
