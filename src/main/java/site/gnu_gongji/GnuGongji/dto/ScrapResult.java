package site.gnu_gongji.GnuGongji.dto;


import lombok.*;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrapResult {

    private Long departmentId;

    private String departmentName;

    private String title;

    private String date;

    private String noticeLink;

    private String uuid;
}
