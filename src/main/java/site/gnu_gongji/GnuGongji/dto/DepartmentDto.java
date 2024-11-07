package site.gnu_gongji.GnuGongji.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentDto {
    private Long departmentId;
    private String departmentKo;
    private String departmentEng;

    public DepartmentDto() {
    }

    public DepartmentDto(Long departmentId, String departmentKo, String departmentEng) {
        this.departmentId = departmentId;
        this.departmentKo = departmentKo;
        this.departmentEng = departmentEng;
    }

    // Getters and Setters
}
