package site.gnu_gongji.GnuGongji.security.oauth2;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import site.gnu_gongji.GnuGongji.exception.OAuth2UnlinkException;

@RequiredArgsConstructor
@Component
public class OAuth2UnlinkManager {

    private final KakaoOAuth2Unlinker kakaoOAuth2Unlinker;

    private final NaverOAuth2Unlinker naverOAuth2Unlinker;

    public void processUnlink(OAuth2Provider provider, String accessToken, OAuth2User oAuth2User) {

        switch (provider) {
            case KAKAO -> kakaoOAuth2Unlinker.unlink(accessToken, oAuth2User);

            case NAVER -> naverOAuth2Unlinker.unlink(accessToken, oAuth2User);

            default -> throw new OAuth2UnlinkException("Unlink to " + provider.getRegistrationId() + " is not support.");
        }
    }
}
