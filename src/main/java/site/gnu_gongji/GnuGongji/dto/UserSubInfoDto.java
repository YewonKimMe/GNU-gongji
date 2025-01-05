package site.gnu_gongji.GnuGongji.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class UserSubInfoDto {

    private Long departmentId;

    private String departmentKo;

    private String departmentEng;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Timestamp startAt;

    public UserSubInfoDto() {
    }

    public UserSubInfoDto(Long departmentId, String departmentKo, String departmentEng, Timestamp startAt) {
        this.departmentId = departmentId;
        this.departmentKo = departmentKo;
        this.departmentEng = departmentEng;
        this.startAt = startAt;
    }
}
