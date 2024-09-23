package site.gnu_gongji.GnuGongji.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString(exclude = "department")
@Table(name = "department_notice_info")
public class DepartmentNoticeInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_info_id")
    private Long noticeInfoId;

    @Column(name = "mi", nullable = false)
    private int mi;

    @Column(name = "bbs_id", nullable = false)
    private int bbsId;

    @Column(name = "last_ntt_sn", nullable = false)
    private int lastNttSn;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
}
