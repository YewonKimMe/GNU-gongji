package site.gnu_gongji.GnuGongji.security.oauth2.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TokenDurationTime {

    ACCESS(3), // 3시간

    REFRESH(1420); // 2개월

    private final long time;
}
