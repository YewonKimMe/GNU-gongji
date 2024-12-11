package site.gnu_gongji.GnuGongji.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import site.gnu_gongji.GnuGongji.entity.CollectedNotifications;
import site.gnu_gongji.GnuGongji.entity.QCollectedNotifications;

import java.util.List;

@Repository
public class CollectedNotificationsRepository {

    private final EntityManager em;

    private final JPAQueryFactory queryFactory;

    public CollectedNotificationsRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public Page<CollectedNotifications> getCollectedNotifications(Pageable pageable, Long departmentId, String noticeTitleQuery) {

        QCollectedNotifications qCollectedNotifications = QCollectedNotifications.collectedNotifications;
        BooleanBuilder builder = new BooleanBuilder(); // 조건
        OrderSpecifier<?> orderSpecifier = null; // 정렬

        orderSpecifier = qCollectedNotifications.id.asc();

        builder.and(qCollectedNotifications.departmentId.eq(departmentId));
        if (StringUtils.isNotBlank(noticeTitleQuery)) {
            builder.and(qCollectedNotifications.notiTitle.contains(noticeTitleQuery));
        }

        List<CollectedNotifications> find = queryFactory.select(qCollectedNotifications)
                .from(qCollectedNotifications)
                .where(builder)
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(qCollectedNotifications.count())
                .from(qCollectedNotifications)
                .where(builder);

        return PageableExecutionUtils.getPage(find, pageable,countQuery::fetchOne);
    }
}
