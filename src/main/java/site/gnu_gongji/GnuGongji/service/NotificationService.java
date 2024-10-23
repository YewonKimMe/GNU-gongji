package site.gnu_gongji.GnuGongji.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import site.gnu_gongji.GnuGongji.dto.FcmNotificationDto;
import site.gnu_gongji.GnuGongji.dto.ScrapResult;
import site.gnu_gongji.GnuGongji.dto.ScrapResultDto;
import site.gnu_gongji.GnuGongji.entity.User;
import site.gnu_gongji.GnuGongji.entity.UserSub;
import site.gnu_gongji.GnuGongji.security.oauth2.enums.Topic;

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

    private final ObjectMapper om = new ObjectMapper();

    //////// 알림 과정 처리 메소드, 실제 발송은 sendNotification ////////
    public void handleNotificationProcess(List<ScrapResultDto> scrapResultDtoList) {

        log.debug("[Execute handleNotificationProcess]");

        log.info("[Execute handleNotificationProcess] result_length={}", scrapResultDtoList.size());

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
                                    fcmService.sendMessage(fcmMessageDto, false);
                                } catch (IOException e) {
                                    log.error("[FCM Message Send IOException], cause={}", e.getMessage());
                                    break;
                                } catch (HttpClientErrorException.NotFound httpClientErrorException) {
                                    handleHttpClientErrorExceptionNotFound(httpClientErrorException, user);
                                    log.error("[FCM Message Send HttpClientErrorException], cause={}", httpClientErrorException.getMessage());
                                    break;
                                } catch (Exception exception) {
                                    log.error("[UnExpectedException Occur], cause={}", exception.getMessage());
                                    break;
                                }
                            }
                        });
            }
        }
    }

    public void handleNotificationProcessWithTopic(List<ScrapResultDto> scrapResultDtoList) {

        log.debug("[Execute handleNotificationProcess]");

        log.info("[Execute handleNotificationProcess] result_length={}", scrapResultDtoList.size());

        for (ScrapResultDto scrapResultDto : scrapResultDtoList) {
            List<ScrapResult> scrapResultList = scrapResultDto.getScrapResultList();
            Long departmentId = scrapResultDto.getDepartmentId();

            for (ScrapResult scrapResult : scrapResultList) {

                String dept_topic = Topic.DEPT_TOPIC_PATH.getPath() + departmentId;
                /// FCM Topic 기반 알림 발송 함수 호출 ///
                fcmService.sendMessageByTopic(scrapResult.getDepartmentName(),
                        scrapResult.getTitle() + " \n(" + scrapResult.getDate() + ")",
                        scrapResult.getNoticeLink(),
                        dept_topic);
            }
        }

    }

    public void handleHttpClientErrorExceptionNotFound(HttpClientErrorException.NotFound e, User user) {
        try {
            JsonNode errorNode = om.readTree(e.getResponseBodyAsString()).path("error");
            int statusCode = errorNode.path("code").asInt();
            String errorMessage = errorNode.path("message").asText();
            String errorCode = errorNode.path("details").get(0).path("errorCode").asText();

            if ("UNREGISTERED".equals(errorCode)) {
                // 토큰 무효화
                log.warn("[FCM Token Unregistered] for user: {}, code: {}, errorCode: {}, details: {}", user.getOauth2Id(), statusCode, errorCode, errorMessage);
                userManageService.invalidateFcmToken(user.getOauth2Id());
                log.info("[USER Token invalidated] user: {}", user.getOauth2Id());
            } else {
                log.error("[FCM NotFound Error] for user {}: {}", user.getOauth2Id(), errorNode.path("message").asText());
            }
        } catch (IOException jsonException) {
            log.error("[Error parsing FCM error response] for user {}: {}", user.getOauth2Id(), jsonException.getMessage());
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
