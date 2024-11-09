package site.gnu_gongji.GnuGongji.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.gnu_gongji.GnuGongji.entity.UserMemoNotification;

import java.util.List;
import java.util.Optional;

public interface UserMemoNotificationRepository extends JpaRepository<UserMemoNotification, Long> {

    @Query("select umn from UserMemoNotification umn where umn.userId = :userId")
    List<UserMemoNotification> getUserMemoNotificationByUserId(@Param("userId") String userId);

    @Query("select umn from UserMemoNotification umn WHERE umn.userId = :userId AND umn.id = :id")
    Optional<UserMemoNotification> findByUserIdAndId(@Param("userId") String userId, @Param("id") Long id);

    @Query("select umn from UserMemoNotification umn WHERE umn.uuid = :uuid")
    Optional<UserMemoNotification> findByUUID(@Param("uuid") byte[] uuid);

    @Modifying
    @Query("delete from UserMemoNotification umn where umn.id = :id AND umn.userId = :userId")
    void deleteUserMemoNotificationByIdAndUserId(@Param("userId") String userId, @Param("id") Long id);
}
