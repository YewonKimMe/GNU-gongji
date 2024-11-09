package site.gnu_gongji.GnuGongji.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmNotificationDto {

    String token;

    String title;

    String body;

    String notificationDate;

    String link;

    String uuid;

    @Builder(toBuilder = true)
    public FcmNotificationDto(String token, String title, String body, String notificationDate, String link, String uuid) {
        this.token = token;
        this.title = title;
        this.body = body;
        this.notificationDate = notificationDate;
        this.link = link;
        this.uuid = uuid;
    }
}
