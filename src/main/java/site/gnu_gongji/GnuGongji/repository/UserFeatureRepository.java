package site.gnu_gongji.GnuGongji.repository;

import site.gnu_gongji.GnuGongji.dto.DepartmentDto;
import site.gnu_gongji.GnuGongji.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserFeatureRepository {

    Optional<User> findUserByOauth2Id(String oauth2Id);

    List<DepartmentDto> findDepartmentsByUserOauth2Id(String oauth2Id);
}
