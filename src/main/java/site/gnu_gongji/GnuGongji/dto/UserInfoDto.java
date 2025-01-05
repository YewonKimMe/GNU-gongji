package site.gnu_gongji.GnuGongji.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoDto {

    private String oauth2Id;

    private String email;

    private String oauth2Provider;

    private String createDate;

    private Integer subLimit;

    private boolean isPushMessagingAgreed;

}
