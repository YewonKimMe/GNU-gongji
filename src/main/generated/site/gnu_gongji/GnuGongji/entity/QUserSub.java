package site.gnu_gongji.GnuGongji.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserSub is a Querydsl query type for UserSub
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserSub extends EntityPathBase<UserSub> {

    private static final long serialVersionUID = 1109638976L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserSub userSub = new QUserSub("userSub");

    public final QUser user;

    public final ListPath<UserSubDepartment, QUserSubDepartment> userSubDepartments = this.<UserSubDepartment, QUserSubDepartment>createList("userSubDepartments", UserSubDepartment.class, QUserSubDepartment.class, PathInits.DIRECT2);

    public final NumberPath<Long> userSubId = createNumber("userSubId", Long.class);

    public QUserSub(String variable) {
        this(UserSub.class, forVariable(variable), INITS);
    }

    public QUserSub(Path<? extends UserSub> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserSub(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserSub(PathMetadata metadata, PathInits inits) {
        this(UserSub.class, metadata, inits);
    }

    public QUserSub(Class<? extends UserSub> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

