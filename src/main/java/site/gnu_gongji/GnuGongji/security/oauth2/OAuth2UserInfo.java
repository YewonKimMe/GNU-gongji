package site.gnu_gongji.GnuGongji.security.oauth2;

import site.gnu_gongji.GnuGongji.security.oauth2.enums.OAuth2Provider;

import java.util.Map;

public interface OAuth2UserInfo {

    OAuth2Provider getProvider();

    String getAccessToken();

    Map<String, Object> getAttributes();

    String getId();

    String getEmail();

    String getName();

    String getFirstName();

    String getLastName();

    String getNickname();

    String getProfileImageUrl();
}
