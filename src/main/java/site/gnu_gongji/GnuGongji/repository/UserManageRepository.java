package site.gnu_gongji.GnuGongji.repository;

import site.gnu_gongji.GnuGongji.entity.User;

import java.util.Optional;

public interface UserManageRepository {

    void createUser(User user);

    Optional<User> findUser(String userEmail);

    boolean deleteUser(String id, String providerName);
}
