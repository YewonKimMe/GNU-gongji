package site.gnu_gongji.GnuGongji.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Getter
@Setter
@Table(name = "user_memo_notification")
public class UserMemoNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId; // oauth2

    private String departmentTitle;

    private String notificationTitle;

    private String link;

    private String time;

    private byte[] encryptedMemo;

    private byte[] uuid;
}
