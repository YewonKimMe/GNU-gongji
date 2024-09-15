package site.gnu_gongji.GnuGongji.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDepartmentNoticeInfo is a Querydsl query type for DepartmentNoticeInfo
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDepartmentNoticeInfo extends EntityPathBase<DepartmentNoticeInfo> {

    private static final long serialVersionUID = -1414772307L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDepartmentNoticeInfo departmentNoticeInfo = new QDepartmentNoticeInfo("departmentNoticeInfo");

    public final NumberPath<Integer> bbsId = createNumber("bbsId", Integer.class);

    public final QDepartment department;

    public final NumberPath<Integer> lastNttSn = createNumber("lastNttSn", Integer.class);

    public final NumberPath<Integer> mi = createNumber("mi", Integer.class);

    public final NumberPath<Long> noticeInfoId = createNumber("noticeInfoId", Long.class);

    public QDepartmentNoticeInfo(String variable) {
        this(DepartmentNoticeInfo.class, forVariable(variable), INITS);
    }

    public QDepartmentNoticeInfo(Path<? extends DepartmentNoticeInfo> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDepartmentNoticeInfo(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDepartmentNoticeInfo(PathMetadata metadata, PathInits inits) {
        this(DepartmentNoticeInfo.class, metadata, inits);
    }

    public QDepartmentNoticeInfo(Class<? extends DepartmentNoticeInfo> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.department = inits.isInitialized("department") ? new QDepartment(forProperty("department")) : null;
    }

}

