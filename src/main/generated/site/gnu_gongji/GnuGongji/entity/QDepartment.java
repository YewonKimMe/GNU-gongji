package site.gnu_gongji.GnuGongji.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDepartment is a Querydsl query type for Department
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDepartment extends EntityPathBase<Department> {

    private static final long serialVersionUID = -1852830041L;

    public static final QDepartment department = new QDepartment("department");

    public final StringPath departmentEng = createString("departmentEng");

    public final NumberPath<Long> departmentId = createNumber("departmentId", Long.class);

    public final StringPath departmentKo = createString("departmentKo");

    public final ListPath<DepartmentNoticeInfo, QDepartmentNoticeInfo> departmentNoticeInfoList = this.<DepartmentNoticeInfo, QDepartmentNoticeInfo>createList("departmentNoticeInfoList", DepartmentNoticeInfo.class, QDepartmentNoticeInfo.class, PathInits.DIRECT2);

    public final ListPath<UserSubDepartment, QUserSubDepartment> userSubDepartmentList = this.<UserSubDepartment, QUserSubDepartment>createList("userSubDepartmentList", UserSubDepartment.class, QUserSubDepartment.class, PathInits.DIRECT2);

    public QDepartment(String variable) {
        super(Department.class, forVariable(variable));
    }

    public QDepartment(Path<? extends Department> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDepartment(PathMetadata metadata) {
        super(Department.class, metadata);
    }

}

