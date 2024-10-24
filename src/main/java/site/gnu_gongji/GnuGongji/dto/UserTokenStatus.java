package site.gnu_gongji.GnuGongji.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTokenStatus {

    private String platform;

    private boolean isAvailable;

    private String createDate;

    @Builder
    public UserTokenStatus(String platform, boolean isAvailable, String createDate) {
        this.platform = platform;
        this.isAvailable = isAvailable;
        this.createDate = createDate;
    }
}
