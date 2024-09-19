package site.gnu_gongji.GnuGongji.security.oauth2.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TokenDurationTime {

    ACCESS(24), // 24시간

    REFRESH(1420); // 2개월

    private final long time;
}
