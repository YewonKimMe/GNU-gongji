package site.gnu_gongji.GnuGongji.security.oauth2.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum JWTClaimKey {
    ISSUER("GNU-GONGJI"),
    TYPE("type"),
    PROVIDER("provider"),
    USERNAME("username"),
    OAUTH2_ID("id"),
    AUTHORITIES("authorities");

    private final String claimKey;
}
