package site.gnu_gongji.GnuGongji.dto;

import lombok.*;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmNotificationDto {

    String token;

    String title;

    String body;

    @Builder(toBuilder = true)
    public FcmNotificationDto(String token, String title, String body) {
        this.token = token;
        this.title = title;
        this.body = body;
    }
}
