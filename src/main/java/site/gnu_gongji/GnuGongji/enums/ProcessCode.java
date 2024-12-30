package site.gnu_gongji.GnuGongji.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ProcessCode {

    SUCCESS("success");

    private final String code;
}
