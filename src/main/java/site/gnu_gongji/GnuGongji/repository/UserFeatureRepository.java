package site.gnu_gongji.GnuGongji.repository;

import site.gnu_gongji.GnuGongji.entity.User;

import java.util.Optional;

public interface UserFeatureRepository {

    Optional<User> findUserByOauth2Id(String oauth2Id);
}
