package site.gnu_gongji.GnuGongji.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.gnu_gongji.GnuGongji.dto.*;
import site.gnu_gongji.GnuGongji.entity.User;
import site.gnu_gongji.GnuGongji.entity.UserMemoNotification;
import site.gnu_gongji.GnuGongji.entity.UserSub;
import site.gnu_gongji.GnuGongji.entity.UserToken;
import site.gnu_gongji.GnuGongji.exception.*;
import site.gnu_gongji.GnuGongji.repository.UserFeatureRepository;
import site.gnu_gongji.GnuGongji.repository.UserManageRepository;
import site.gnu_gongji.GnuGongji.repository.UserMemoNotificationRepository;
import site.gnu_gongji.GnuGongji.security.oauth2.enums.Topic;
import site.gnu_gongji.GnuGongji.tool.AESUtil;
import site.gnu_gongji.GnuGongji.tool.UUIDConverter;

import java.text.SimpleDateFormat;
import java.util.*;

@Transactional
@RequiredArgsConstructor
@Slf4j
@Service
public class UserFeatureService {

    private final DepartmentService departmentService;

    private final UserFeatureRepository userFeatureRepository;

    private final UserManageRepository userManageRepository;

    private final FcmService fcmService;

    private final AESUtil aesUtil;

    private final UserMemoNotificationRepository userMemoNotificationRepository;

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
        return fcmService.subscribeTopic(tokens, Topic.DEPT_TOPIC_PATH.getPath() + departmentId);
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

        if (findUser.getUserTokens() == null || findUser.getUserTokens().isEmpty()) return false;

        Set<UserToken> userTokens = findUser.getUserTokens();
        int tokenNum = userTokens.size();
        int result = 0;

        for (UserToken userToken : userTokens) {
            result += fcmService.sendMessage(
                    new FcmNotificationDto(userToken.getToken(), "테스트 제목", "테스트 본문", "2024.01.01","https://gnu-gongji.pages.dev", UUID.randomUUID().toString()),
                    true
            );
        }

        return result == tokenNum;
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
    public List<UserTokenStatus> getUserDevicesAndStatus(String oAuth2Id) {

        List<UserTokenStatus> tokenStatuses = new LinkedList<>();

        // 유저와 user_tokens 를 가져와서
        User user = userManageRepository.findUserByOAuth2Id(oAuth2Id)
                .orElseThrow(() -> new UserNotExistException("해당 유저로 검색된 계정이 없습니다."));

        Set<UserToken> userTokens = user.getUserTokens();


        // fcm 에서 반복하며 유효성을 검사함
        for (UserToken userToken : userTokens) {
            FcmNotificationDto fcmNotificationDto = FcmNotificationDto.builder()
                    .title("test")
                    .body("test")
                    .token(userToken.getToken())
                    .build();
            int result = fcmService.sendMessage(fcmNotificationDto, true);

            // SimpleDateFormat을 사용하여 포맷 지정
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String formattedDate = formatter.format(userToken.getAddDate());

            UserTokenStatus status = UserTokenStatus.builder()
                    .platform(userToken.getPlatform())
                    .isAvailable(result == 1)
                    .createDate(formattedDate)
                    .build();

            // 리스트에 DTO 객체{platform, status, 추가일자}를 추가
            tokenStatuses.add(status);
        }
        return tokenStatuses;
    }

    public void saveUserMemoNotification(UserMemoNotificationDto userMemoNotificationDto, Authentication authentication) {

        Optional<UserMemoNotification> findUmnOpt = userMemoNotificationRepository.findByUUID(UUIDConverter.convertUuidStringToBinary16(userMemoNotificationDto.getUuid()));

        if (findUmnOpt.isPresent()) throw new DuplicationException("이미 동기화(저장)된 공지사항입니다.");

        try {
            byte[] encryptMemo = aesUtil.encrypt(userMemoNotificationDto.getMemo());
            UserMemoNotification userMemoNotification = UserMemoNotification.builder()
                    .encryptedMemo(encryptMemo) // 메모 암호화
                    .departmentTitle(userMemoNotificationDto.getDepartmentTitle())
                    .notificationTitle(userMemoNotificationDto.getNotificationTitle())
                    .time(userMemoNotificationDto.getTime())
                    .link(userMemoNotificationDto.getLink())
                    .userId(authentication.getName())
                    .uuid(UUIDConverter.convertUuidStringToBinary16(userMemoNotificationDto.getUuid()))
                    .build();

            userMemoNotificationRepository.save(userMemoNotification);

            log.debug("decode AES String={}", aesUtil.decrypt(encryptMemo));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<UserMemoNotificationDto> getUserMemoNotifications(String oAuth2Id) {
        log.debug("GetUserMemoNotifications: oAuth2Id={}", oAuth2Id);
        List<UserMemoNotification> findUserMemoNotifications = userMemoNotificationRepository.getUserMemoNotificationByUserId(oAuth2Id);

        List<UserMemoNotificationDto> list = new ArrayList<>();
        findUserMemoNotifications
                .forEach(userMemoNotification -> {
                    try {
                        UserMemoNotificationDto dto =  UserMemoNotificationDto.builder()
                                .id(userMemoNotification.getId())
                                .departmentTitle(userMemoNotification.getDepartmentTitle())
                                .notificationTitle(userMemoNotification.getNotificationTitle())
                                .memo(aesUtil.decrypt(userMemoNotification.getEncryptedMemo()))
                                .link(userMemoNotification.getLink())
                                .time(userMemoNotification.getTime())
                                .uuid(UUIDConverter.convertBinary16ToUUID(userMemoNotification.getUuid()).toString())
                                .build();
                        list.add(dto);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        return list;
    }

    public void updateUserMemoNotification(Long id, UserMemoNotificationDto userMemoNotificationDto, Authentication authentication) {
        UserMemoNotification userMemoNotification = userMemoNotificationRepository.findByUserIdAndId(authentication.getName(), id)
                .orElseThrow(() -> new MemoNotExistException("해당 유저와 공지사항 ID로 검색된 동기화된 공지사항이 없습니다."));
        try {
            userMemoNotification.setEncryptedMemo(aesUtil.encrypt(userMemoNotificationDto.getMemo()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void deleteUserMemoNotification(Long id, Authentication authentication) {
        userMemoNotificationRepository.deleteUserMemoNotificationByIdAndUserId(authentication.getName(), id);
    }
}
