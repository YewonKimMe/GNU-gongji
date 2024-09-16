package site.gnu_gongji.GnuGongji.security.oauth2;

import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2Unlinker {

    void unlink(String accessToken, OAuth2User oAuth2User);

}
