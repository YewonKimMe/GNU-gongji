package site.gnu_gongji.GnuGongji.security.oauth2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import site.gnu_gongji.GnuGongji.exception.OAuth2AuthenticationProcessingException;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processOAut2User(userRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception e) {
            throw new InternalAuthenticationServiceException(e.getMessage(), e.getCause());
        }
    }

    private OAuth2User processOAut2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {

        // 클라이언트 등록 정보 획득
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 엑세스 토큰 획득
        String accessToken = userRequest.getAccessToken().getTokenValue();

        log.debug("registrationId={}, accessToken={}", registrationId, accessToken);

        // 등록 정보, 엑세스 토큰 기반으로 OAuth2 제공자 별 UserInfo 가져오기
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAtu2UserInfo(registrationId, accessToken, oAuth2User.getAttributes());

        // OAuth2UserInfo field value validation
        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }
        return new OAuth2UserPrincipal(oAuth2UserInfo);
    }
}
