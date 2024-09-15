package site.gnu_gongji.GnuGongji.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "department")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "department_ko")
    private String departmentKo;

    @Column(name = "department_eng")
    private String departmentEng;

    @OneToMany(mappedBy = "department")
    private List<UserSubDepartment> userSubDepartmentList = new ArrayList<>();

    @OneToMany(mappedBy = "department")
    private List<DepartmentNoticeInfo> departmentNoticeInfoList = new ArrayList<>();
}
