package site.gnu_gongji.GnuGongji.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.gnu_gongji.GnuGongji.dto.UserCreateDto;
import site.gnu_gongji.GnuGongji.entity.Authority;
import site.gnu_gongji.GnuGongji.entity.User;
import site.gnu_gongji.GnuGongji.exception.UserNotExistException;
import site.gnu_gongji.GnuGongji.repository.UserManageRepository;
import site.gnu_gongji.GnuGongji.security.oauth2.enums.OAuth2Provider;
import site.gnu_gongji.GnuGongji.security.oauth2.OAuth2UserPrincipal;
import site.gnu_gongji.GnuGongji.security.oauth2.enums.Role;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Transactional
@RequiredArgsConstructor
@Slf4j
@Service
public class UserManageService {

    @Value("${mycustom.sub-limit}")
    private int subLimit;

    private final UserManageRepository userManageRepository;

    public UserCreateDto createOAuth2User(OAuth2UserPrincipal oAuth2UserPrincipal, String refreshToken) {

        String userEmail = oAuth2UserPrincipal.getUserInfo().getEmail();
        String oAuth2Provider = oAuth2UserPrincipal.getUserInfo().getProvider().getRegistrationId();
        Timestamp createDate = new Timestamp(System.currentTimeMillis());

        if (this.isUserAlreadyExist(userEmail)) {
            return new UserCreateDto(null, null, true, null);
        }

        User user = User.builder()
                .userEmail(userEmail)
                .isOAuth2(true)
                .oauth2Provider(oAuth2Provider)
                .oauth2Id(oAuth2UserPrincipal.getUserInfo().getId())
                .refreshToken(refreshToken)
                .authorities(new HashSet<>())
                .createDate(createDate)
                .subLimit(subLimit)
                .build();

        Authority authority = Authority.builder()
                .role(Role.PREFIX.getValue() + Role.USER.getValue())
                .build();

        user.getAuthorities().add(authority);

        authority.setUser(user);

        userManageRepository.createUser(user);

        log.info("[USER CREATED] email={}, provider={}", userEmail, oAuth2Provider);

        return new UserCreateDto(userEmail, oAuth2Provider, false, createDate);
    }

    public boolean isUserAlreadyExist(String userEmail) {

        Optional<User> userOptional = userManageRepository.findUserByEmail(userEmail);

        return userOptional.isPresent();
    }

    public boolean deleteOAuth2User(String oauth2Id, OAuth2Provider provider) {

        return userManageRepository.deleteUser(oauth2Id, provider.getRegistrationId());
    }

    public Optional<User> findOAuth2User(String oauth2Id, String oauth2Provider) {
        return userManageRepository.findUserByOauth2IdAndOAuth2Provider(oauth2Id, oauth2Provider);
    }

    public boolean updateRefreshToken(String oauth2Id, String oauth2Provider, String newRefreshToken) {
        return userManageRepository.updateRefreshToke(oauth2Id, oauth2Provider, newRefreshToken);
    }

    public List<User> findUsersWithActiveSubscriptionsAndNotifications() {

        return userManageRepository.findUsersWithActiveSubscriptionsAndNotifications()
                .orElseThrow(() -> new UserNotExistException("구독, 알림 설정중인 유저가 없습니다."));
    }

    public void updateUserFcmToken(String userOAuth2Id, String newFcmToken) {
        User user = userManageRepository.findUserByOAuth2Id(userOAuth2Id)
                .orElseThrow(() -> new UserNotExistException("해당 ID로 검색된 유저가 없습니다."));
        user.setFcmToken(newFcmToken);
    }

    public void invalidateFcmToken(String oAuth2Id) {
        User findUser = userManageRepository.findUserByOAuth2Id(oAuth2Id)
                .orElseThrow(() -> new UserNotExistException("해당 ID로 검색된 유저가 없습니다."));

        findUser.setRefreshToken(null);

    }
}
