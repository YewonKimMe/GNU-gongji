package site.gnu_gongji.GnuGongji.repository;

import site.gnu_gongji.GnuGongji.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserManageRepository {

    void createUser(User user);

    Optional<User> findUserByEmail(String userEmail);

    Optional<User> findUserByOauth2IdAndOAuth2Provider(String oauth2Id, String oauth2Provider);

    Optional<User> findUserByOAuth2Id(String oAuth2Id);

    boolean deleteUser(String id, String providerName);

    void deleteUserMemo(String id);

    boolean updateRefreshToke(String oauth2Id, String oauth2Provider, String newRefreshToken);

    Optional<List<User>> findUsersWithActiveSubscriptionsAndNotifications();
}
