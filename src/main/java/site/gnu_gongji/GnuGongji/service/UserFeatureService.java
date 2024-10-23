package site.gnu_gongji.GnuGongji.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import site.gnu_gongji.GnuGongji.dto.DepartmentDto;
import site.gnu_gongji.GnuGongji.dto.EmailDto;
import site.gnu_gongji.GnuGongji.dto.FcmNotificationDto;
import site.gnu_gongji.GnuGongji.dto.UserInfoDto;
import site.gnu_gongji.GnuGongji.entity.User;
import site.gnu_gongji.GnuGongji.entity.UserSub;
import site.gnu_gongji.GnuGongji.entity.UserToken;
import site.gnu_gongji.GnuGongji.exception.*;
import site.gnu_gongji.GnuGongji.repository.UserFeatureRepository;
import site.gnu_gongji.GnuGongji.security.oauth2.enums.Topic;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Transactional
@RequiredArgsConstructor
@Slf4j
@Service
public class UserFeatureService {

    private final DepartmentService departmentService;

    private final UserFeatureRepository userFeatureRepository;

    private final FcmService fcmService;

    // 이메일 변경
    public void updateUserEmail(final String userOAuth2Id, final EmailDto emailDto) {

        User findUser = userFeatureRepository.findUserByOauth2Id(userOAuth2Id)
                .orElseThrow(() -> new UserNotExistException("해당 ID로 조회된 유저가 없습니다."));

        findUser.setUserEmail(emailDto.getEmail());
    }

    // 유저 공지사항 구독 추가
    public int addUserSubDepartment(final String userOAuth2Id, final Long departmentId) {

        // 유효한 부서 여부 확인
        checkDepartment(departmentId);

        // 유저 조회
        Optional<User> findUserOpt = userFeatureRepository.findUserByOauth2Id(userOAuth2Id);

        if (findUserOpt.isEmpty()) {
            throw new UserNotExistException("해당 ID로 조회된 유저가 없습니다.");
        }

        User findUser = findUserOpt.get();

        // userSub 에 해당 deptId 존재 여부 체크(중복 방지)
        findUser.getSubList()
                .stream()
                .filter(userSub -> userSub.getDepartmentId().equals(departmentId))
                .findFirst()
                .ifPresent(sub -> {throw new DuplicateSubscribeException("이미 구독중인 학과/부서입니다.");
                });

        int userSubSize = findUser.getSubList().size();

        // 유저의 구독 갯수 제한 조회
        int limit = findUser.getSubLimit();

        if (userSubSize >= limit) {
            throw new SubscriptionLimitReachedException("해당 계정의 공지사항 구독은 최대 " + limit +"개 까지만 가능합니다.\n현재 구독중인 갯수는 " + userSubSize + "개 입니다.");
        }

        UserSub userSub = new UserSub();

        userSub.setDepartmentId(departmentId);
        userSub.setUser(findUser);

        findUser.getSubList().add(userSub);

        List<String> tokens = new ArrayList<>();
        Set<UserToken> userTokens = findUser.getUserTokens();
        for (UserToken userToken : userTokens) {
            tokens.add(userToken.getToken());
        }

        // FCM
        int subCnt = fcmService.subscribeTopic(tokens, Topic.DEPT_TOPIC_PATH.getPath() + departmentId);

        return subCnt;
    }

    // 구독중인 유저 공지사항 삭제
    public int deleteSubscription(final String userOAuth2Id, final Long departmentId) {

        // 유효한 여부 확인
        checkDepartment(departmentId);

        // 유저 조회
        Optional<User> findUserOpt = userFeatureRepository.findUserByOauth2Id(userOAuth2Id);

        // 조회 결과가 없는 경우
        if (findUserOpt.isEmpty()) {
            throw new UserNotExistException("해당 ID로 조회된 유저가 없습니다.");
        }

        // OAuth2Id 로 조회된 유저
        User findUser = findUserOpt.get();

        UserSub findUserSub = findUserOpt.get().getSubList()
                .stream()
                .filter(userSub -> userSub.getDepartmentId().equals(departmentId))
                .findFirst()
                .orElseThrow(() -> new NotExistKeyException("해당 ID로 조회된 구독 정보가 없습니다."));

        // UserSubList 에서 해당 userSub 제거
        findUser.getSubList().remove(findUserSub);

        // 양방향 관계 정리
        findUserSub.setUser(null);

        List<String> tokens = new ArrayList<>();
        Set<UserToken> userTokens = findUser.getUserTokens();
        for (UserToken userToken : userTokens) {
            tokens.add(userToken.getToken());
        }
        // fcm 정리
        return fcmService.unSubscribeTopic(tokens, Topic.DEPT_TOPIC_PATH.getPath() + departmentId);
    }

    private void checkDepartment(Long departmentId) {
        // Department 조회
        if (!departmentService.checkDepartmentExistByDId(departmentId)) {
            throw new DepartmentNotExistException("해당 ID로 검색된 학과/부서가 없습니다.");
        }
    }

    public List<DepartmentDto> getUserSub(String oauth2Id) {
        return userFeatureRepository.findDepartmentsByUserOauth2Id(oauth2Id);
    }

    public boolean checkUserFCMToken(String oAuth2Id) {
        User findUser = userFeatureRepository.findUserByOauth2Id(oAuth2Id)
                .orElseThrow(() -> new UserNotExistException("조회된 유저 정보가 없습니다."));

        if (findUser.getFcmToken() == null) return false;

        try {
            fcmService.sendMessage(
                    new FcmNotificationDto(findUser.getFcmToken(), "테스트 제목", "테스트 본문", "https://gnu-gongji.pages.dev"),
                    true
            );
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public UserInfoDto getUserInfoByOAuthId(String oAuth2Id) {
        User findUser = userFeatureRepository.findUserByOauth2Id(oAuth2Id)
                .orElseThrow(() -> new UserNotExistException("조회된 유저 정보가 없습니다."));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String formattedDate = sdf.format(findUser.getCreateDate());

        return UserInfoDto.builder()
                .oauth2Id(findUser.getOauth2Id())
                .email(findUser.getUserEmail())
                .oauth2Provider(findUser.getOauth2Provider())
                .createDate(formattedDate)
                .isPushMessagingAgreed(findUser.getFcmToken() != null)
                .build();
    }
}
