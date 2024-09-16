package site.gnu_gongji.GnuGongji.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SecurityConst {

    AUTH_HEADER("Authorization"),

    BEARER_PREFIX("Bearer ");

    private final String value;
}
