package site.gnu_gongji.GnuGongji.dto;

import lombok.*;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmTokenDto {

    private String token;

    @Builder(toBuilder = true)
    public FcmTokenDto(String token) {
        this.token = token;
    }
}
