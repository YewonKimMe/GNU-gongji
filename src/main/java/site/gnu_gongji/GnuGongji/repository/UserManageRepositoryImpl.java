package site.gnu_gongji.GnuGongji.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import site.gnu_gongji.GnuGongji.entity.QUser;
import site.gnu_gongji.GnuGongji.entity.User;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class UserManageRepositoryImpl implements UserManageRepository {

    private final EntityManager em;

    private final JPAQueryFactory queryFactory;

    public UserManageRepositoryImpl(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public void createUser(User user) {
        em.persist(user);
    }

    @Override
    public Optional<User> findUserByEmail(String userEmail) {

        QUser user = QUser.user;
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(user.userEmail.eq(userEmail));

        User findUser = queryFactory
                .select(user)
                .from(user)
                .where(builder)
                .fetchOne();

        return Optional.ofNullable(findUser);
    }

    @Override
    public Optional<User> findUserByOauth2IdAndOAuth2Provider(String oauth2Id, String oauth2Provider) {

        QUser user = QUser.user;
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(user.isOAuth2.isTrue());
        builder.and(user.oauth2Id.eq(oauth2Id));
        builder.and(user.oauth2Provider.eq(oauth2Provider));

        User findUser = queryFactory
                .select(user)
                .from(user)
                .where(builder)
                .fetchOne();

        return Optional.ofNullable(findUser);
    }

    @Override
    public boolean deleteUser(String id, String providerName) {
        QUser user = QUser.user;
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(user.oauth2Id.eq(id));
        builder.and(user.oauth2Provider.eq(providerName));

        long result = queryFactory
                .delete(user)
                .where(builder)
                .execute();
        return result > 0;
    }

    @Override
    public boolean updateRefreshToke(String oauth2Id, String oauth2Provider, String newRefreshToken) {
        QUser user = QUser.user;
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(user.oauth2Id.eq(oauth2Id));
        builder.and(user.oauth2Provider.eq(oauth2Provider));

        User findUser = queryFactory
                .select(user)
                .from(user)
                .where(builder)
                .fetchOne();
        log.debug("User={}", user);
        if (null == findUser) {
            return false;
        }

        findUser.setRefreshToken(newRefreshToken);
        return true;
    }

    @Override
    public Optional<List<User>> findUsersWithActiveSubscriptionsAndNotifications() {
        // TODO userSub, userToken 이 존재하는 유저만 가져오도록 수정
        List<User> resultList = em.createQuery("SELECT u FROM User u JOIN FETCH u.subList WHERE u.subList IS NOT EMPTY")
                .getResultList();
        return resultList.isEmpty() ? Optional.empty() : Optional.of(resultList);
    }
}
