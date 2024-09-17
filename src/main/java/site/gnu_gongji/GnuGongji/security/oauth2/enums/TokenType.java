package site.gnu_gongji.GnuGongji.security.oauth2.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TokenType {

    ACCESS("Access-Token"),

    REFRESH("Refresh-Token");

    private final String tokenName;
}
