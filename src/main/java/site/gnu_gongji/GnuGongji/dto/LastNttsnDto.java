package site.gnu_gongji.GnuGongji.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LastNttsnDto {

    private Long id;

    private Integer nttSn;

    @Builder
    public LastNttsnDto(Long id, Integer nttSn) {
        this.id = id;
        this.nttSn = nttSn;
    }
}
