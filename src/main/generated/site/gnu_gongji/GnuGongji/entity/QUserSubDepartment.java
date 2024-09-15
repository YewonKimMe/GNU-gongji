package site.gnu_gongji.GnuGongji.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserSubDepartment is a Querydsl query type for UserSubDepartment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserSubDepartment extends EntityPathBase<UserSubDepartment> {

    private static final long serialVersionUID = -1236414798L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserSubDepartment userSubDepartment = new QUserSubDepartment("userSubDepartment");

    public final QDepartment department;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QUserSub userSub;

    public QUserSubDepartment(String variable) {
        this(UserSubDepartment.class, forVariable(variable), INITS);
    }

    public QUserSubDepartment(Path<? extends UserSubDepartment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserSubDepartment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserSubDepartment(PathMetadata metadata, PathInits inits) {
        this(UserSubDepartment.class, metadata, inits);
    }

    public QUserSubDepartment(Class<? extends UserSubDepartment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.department = inits.isInitialized("department") ? new QDepartment(forProperty("department")) : null;
        this.userSub = inits.isInitialized("userSub") ? new QUserSub(forProperty("userSub"), inits.get("userSub")) : null;
    }

}

