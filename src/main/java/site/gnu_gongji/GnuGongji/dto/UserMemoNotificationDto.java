package site.gnu_gongji.GnuGongji.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserMemoNotificationDto {

    private Long id;

    private String departmentTitle;

    private String notificationTitle;

    private String link;

    private String time; // notificationDate

    private String memo;
}
