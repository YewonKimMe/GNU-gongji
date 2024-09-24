package site.gnu_gongji.GnuGongji.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@Table(name = "user_sub")
public class UserSub {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_sub_id")
    private Long userSubId;

//    @Column(name = "department_eng")
//    private String departmentEng;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "departmentId")
    private Long departmentId;

//    @OneToMany(mappedBy = "userSub")
//    private List<UserSubDepartment> userSubDepartments = new ArrayList<>();
}
