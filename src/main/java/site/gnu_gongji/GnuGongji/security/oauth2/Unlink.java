package site.gnu_gongji.GnuGongji.security.oauth2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Unlink {

    KAKAO("https://kapi.kakao.com/v1/user/unlink"),

    NAVER("https://nid.naver.com/oauth2.0/token");

    private final String url;
}
