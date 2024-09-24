package site.gnu_gongji.GnuGongji.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.gnu_gongji.GnuGongji.entity.User;

import java.util.Optional;

public interface JpaUserFeatureRepository extends UserFeatureRepository, JpaRepository<User, Long> {

    @Query("SELECT u from User u WHERE u.oauth2Id = :oauth2Id")
    Optional<User> findUserByOauth2Id(@Param("oauth2Id") String oauth2Id);
}
