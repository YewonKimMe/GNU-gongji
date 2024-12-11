package site.gnu_gongji.GnuGongji.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "collected_notification")
public class CollectedNotifications {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long departmentId;

    private String departmentName;

    private String notiTitle;

    private String dateTime;

    private String link;

    @Column(name = "created_time")
    private Timestamp createTime;

    private byte[] uuid;

    @Builder
    public CollectedNotifications(Long id, Long departmentId, String departmentName, String notiTitle, String dateTime, String link) {
        this.id = id;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.notiTitle = notiTitle;
        this.dateTime = dateTime;
        this.link = link;
    }
}
