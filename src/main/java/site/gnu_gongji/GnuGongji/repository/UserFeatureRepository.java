package site.gnu_gongji.GnuGongji.repository;

import site.gnu_gongji.GnuGongji.dto.DepartmentDto;
import site.gnu_gongji.GnuGongji.dto.UserSubInfoDto;
import site.gnu_gongji.GnuGongji.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserFeatureRepository {

    Optional<User> findUserByOauth2Id(String oauth2Id);

    List<UserSubInfoDto> findDepartmentsByUserOauth2Id(String oauth2Id);
}
