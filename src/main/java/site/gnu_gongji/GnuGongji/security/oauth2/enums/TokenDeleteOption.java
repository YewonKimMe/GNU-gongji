package site.gnu_gongji.GnuGongji.security.oauth2.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TokenDeleteOption {

    ALL("all"),
    SPECIFIC("specific");

    private final String option;

    public static TokenDeleteOption getOption(String value) {
        for (TokenDeleteOption tokenDeleteOption : values()) {
            if (tokenDeleteOption.getOption().equals(value)) {
                return tokenDeleteOption;
            }
        }
        throw new IllegalArgumentException("Illegal Token Delete Option");
    }
}
