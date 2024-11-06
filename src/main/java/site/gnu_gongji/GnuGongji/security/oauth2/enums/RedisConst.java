package site.gnu_gongji.GnuGongji.security.oauth2.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisConst {

    DEPARTMENT_DTO("DepartmentDto:List");

    private final String value;
}
