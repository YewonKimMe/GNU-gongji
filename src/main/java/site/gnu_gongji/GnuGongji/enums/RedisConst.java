package site.gnu_gongji.GnuGongji.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisConst {

    DEPARTMENT_DTO("DepartmentDto:List");

    private final String value;
}
