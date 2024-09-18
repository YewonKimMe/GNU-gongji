package site.gnu_gongji.GnuGongji.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDto {

    private String userEmail;

    private String userOAuth2Provider;

    private boolean isDuplicated;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Timestamp createDate;
}
