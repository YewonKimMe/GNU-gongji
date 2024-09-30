package site.gnu_gongji.GnuGongji.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.gnu_gongji.GnuGongji.dto.FcmNotificationDto;
import site.gnu_gongji.GnuGongji.dto.ScrapResult;
import site.gnu_gongji.GnuGongji.dto.ScrapResultDto;
import site.gnu_gongji.GnuGongji.entity.User;
import site.gnu_gongji.GnuGongji.entity.UserSub;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {


    private final UserManageService userManageService;

    private final FcmService fcmService;

    //////// 알림 과정 처리 메소드, 실제 발송은 sendNotification ////////
    public void handleNotificationProcess(List<ScrapResultDto> scrapResultDtoList) {

        log.debug("[Execute handleNotificationProcess]");

        List<User> userList = userManageService.findUsersWithActiveSubscriptionsAndNotifications(); // 사용자 리스트, 길이는 n

        for (ScrapResultDto scrapResultDto : scrapResultDtoList) { // 최대 200개 내외임을 보장
            
            Long departmentId = scrapResultDto.getDepartmentId();

            /*
            * scrapResultList 는 항상 scrapResultDto 의 departmentId 와 일치하는 요소들이 존재함이 보장됨.
            * 한 부서 내에 여러개의 공지사항 탭이 존재하는 경우에 ScrapResult 가 scrapResultList 에 저장됨
            * scrapResultList 는 아무리 많아봐야 20개 내외( 한 부서의 공지사항 1페이지는 최대 20개 내외 )
            * */
            List<ScrapResult> scrapResultList = scrapResultDto.getScrapResultList();
            
            for (User user : userList) {
                Set<UserSub> subList = user.getSubList(); // 최대 3개 요소만 존재함을 보장

                subList.stream()
                        .filter(userSub -> userSub.getDepartmentId().equals(departmentId))
                        .findFirst()
                        .ifPresent(findUserSub -> {
                            for (ScrapResult scrapResult : scrapResultList) {
                                FcmNotificationDto fcmMessageDto = createFcmMessageDto(scrapResult, user);

                                //////// FCM Push Message 전송 ////////
                                try {
                                    fcmService.sendMessage(fcmMessageDto);
                                } catch (IOException e) {
                                    log.error("[FCM Message Send Exception], cause={}", e.getMessage());
                                    throw new RuntimeException(e);
                                }
                            }
                        });
            }
        }
    }

    // fcmNotificationDto create
    /*
    * userFcmToken
    * title - 학과명
    * body - 공지사항 제목
    * link - 공지사항 세부 링크
    * */
    public FcmNotificationDto createFcmMessageDto(ScrapResult scrapResult, User user) {

        return FcmNotificationDto.builder()
                .token(user.getFcmToken())
                .title(scrapResult.getDepartmentName())
                .body(scrapResult.getTitle() + " \n(" + scrapResult.getDate() + ")")
                .link(scrapResult.getNoticeLink())
                .build();
    }
}
