package site.gnu_gongji.GnuGongji.security.oauth2.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Role {

    PREFIX("ROLE_"),

    USER("USER"),

    ADMIN("ADMIN");

    private final String value;
}
