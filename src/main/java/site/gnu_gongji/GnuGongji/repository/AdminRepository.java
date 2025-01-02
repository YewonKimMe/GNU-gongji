package site.gnu_gongji.GnuGongji.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import site.gnu_gongji.GnuGongji.entity.QUser;

@Repository
public class AdminRepository {

    private final EntityManager em;

    private final JPAQueryFactory queryFactory;

    public AdminRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public void updateSubscribeLimit(Integer subscribeLimit) {
        QUser user = QUser.user;

        queryFactory
                .update(user)
                .set(user.subLimit, subscribeLimit)
                .execute();
    }
}
