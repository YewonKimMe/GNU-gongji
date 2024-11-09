package site.gnu_gongji.GnuGongji.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.gnu_gongji.GnuGongji.dto.UserCreateDto;
import site.gnu_gongji.GnuGongji.entity.Authority;
import site.gnu_gongji.GnuGongji.entity.User;
import site.gnu_gongji.GnuGongji.entity.UserSub;
import site.gnu_gongji.GnuGongji.entity.UserToken;
import site.gnu_gongji.GnuGongji.exception.UserNotExistException;
import site.gnu_gongji.GnuGongji.repository.UserManageRepository;
import site.gnu_gongji.GnuGongji.security.oauth2.enums.*;
import site.gnu_gongji.GnuGongji.security.oauth2.OAuth2UserPrincipal;

import java.sql.Timestamp;
import java.util.*;

@Transactional
@RequiredArgsConstructor
@Slf4j
@Service
public class UserManageService {

    @Value("${mycustom.sub-limit}")
    private int subLimit;

    private final UserManageRepository userManageRepository;

    private final FcmService fcmService;

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

    // 유저 탈퇴 함수
    public boolean deleteOAuth2User(String oauth2Id, OAuth2Provider provider) {

        // 유저 메모 제거
        userManageRepository.deleteUserMemo(oauth2Id);

        // 유저 정보 및 연계된 모든 정보 제거
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

    public void updateUserFcmToken(String userOAuth2Id, String newFcmToken, Device device) {
        User user = userManageRepository.findUserByOAuth2Id(userOAuth2Id)
                .orElseThrow(() -> new UserNotExistException("해당 ID로 검색된 유저가 없습니다."));
        user.setFcmToken(newFcmToken);

        // 유저 디바이스별 토큰을 등록하거나 새로 추가
        Optional<UserToken> findUserToken = user.getUserTokens().stream()
                .filter(token -> token.getPlatform().equals(device.getDevice()))
                .findFirst();

        if (findUserToken.isPresent()) {
            findUserToken.get().setToken(newFcmToken);
            findUserToken.get().setAddDate(new Timestamp(System.currentTimeMillis()));
            List<String> userTokens = new ArrayList<>();
            for (UserToken userToken : user.getUserTokens()) {
                userTokens.add(userToken.getToken());
            }
            // FCM Topic 재등록
            user.getSubList()
                    .forEach(sub -> fcmService.subscribeTopic(userTokens, Topic.DEPT_TOPIC_PATH.getPath() + sub.getDepartmentId()));
        } else {
            UserToken newUserToken = new UserToken();
            newUserToken.setUser(user);
            newUserToken.setPlatform(device.getDevice());
            newUserToken.setToken(newFcmToken);
            newUserToken.setAddDate(new Timestamp(System.currentTimeMillis()));
            user.getUserTokens().add(newUserToken);

            log.debug("newFcmToken={}", newFcmToken);
            user.getSubList()
                    .forEach(sub -> fcmService.subscribeTopic(List.of(newFcmToken), Topic.DEPT_TOPIC_PATH.getPath() + sub.getDepartmentId()));
        }
    }

    public void deleteUserFcmToken(String oAuth2Id, String option, String platform) {

        TokenDeleteOption deleteOption = TokenDeleteOption.getOption(option);

        Device deviceInfo = Device.getDevice(platform);

        // FCM에서 삭제
        User findUser = userManageRepository.findUserByOAuth2Id(oAuth2Id)
                .orElseThrow(() -> new UserNotExistException("해당 ID로 검색된 유저가 없습니다."));

        Set<UserSub> subList = findUser.getSubList();

        switch (deleteOption) {
            case ALL -> {
                // fcm 구독 해제 처리
                log.debug("case ALL");

                for (UserSub userSub : subList) {
                    Long departmentId = userSub.getDepartmentId();
                    String topic = Topic.DEPT_TOPIC_PATH.getPath() + departmentId;
                    List<String> tokens = new ArrayList<>();
                    for (UserToken userToken : findUser.getUserTokens()) {
                        tokens.add(userToken.getToken());
                    }
                    fcmService.unSubscribeTopic(tokens, topic);
                }
                // db 삭제
                findUser.getUserTokens().clear();

            }
            case SPECIFIC -> {
                log.debug("case SPECIFIC");
                /*
                * 1. userTokens 를 기준으로 반복 시작
                * 2. userSub 로 2차 반복 시작
                *
                * */
                List<UserToken> tokensToRemove = new ArrayList<>();
                for (UserToken userToken : findUser.getUserTokens()) {

                    if (!userToken.getPlatform().trim().equalsIgnoreCase(deviceInfo.getDevice().trim())) continue;

                    for (UserSub userSub : subList) {
                        String topic = Topic.DEPT_TOPIC_PATH.getPath() + userSub.getDepartmentId();

                        String fcmToken = userToken.getToken();
                        fcmService.unSubscribeTopic(List.of(fcmToken), topic);
                    }
                    // db 삭제 - userToken 관계 해소
                    userToken.setToken(null);
                    userToken.setUser(null);
                    tokensToRemove.add(userToken);
                    // TODO 나중에 제거
                    findUser.setFcmToken(null);
                }
                // db 삭제 - 부모 엔티티에서 관계 해소
                tokensToRemove.forEach(findUser.getUserTokens()::remove);
            }
        }
    }

    public void invalidateFcmToken(String oAuth2Id) {
        User findUser = userManageRepository.findUserByOAuth2Id(oAuth2Id)
                .orElseThrow(() -> new UserNotExistException("해당 ID로 검색된 유저가 없습니다."));

        findUser.setFcmToken(null);
    }
}
