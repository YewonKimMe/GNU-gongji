package site.gnu_gongji.GnuGongji.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrapResultDto {

    private Long departmentId;

    private String departmentName;

    private List<ScrapResult> scrapResultList;

}
