package site.gnu_gongji.GnuGongji.service;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.gnu_gongji.GnuGongji.dto.LastNttsnDto;
import site.gnu_gongji.GnuGongji.dto.ScrapNotification;
import site.gnu_gongji.GnuGongji.dto.ScrapResult;
import site.gnu_gongji.GnuGongji.dto.ScrapResultDto;
import site.gnu_gongji.GnuGongji.entity.Department;
import site.gnu_gongji.GnuGongji.entity.DepartmentNoticeInfo;
import site.gnu_gongji.GnuGongji.enums.Topic;
import site.gnu_gongji.GnuGongji.repository.CollectedNotificationsJDBCRepository;
import site.gnu_gongji.GnuGongji.repository.DepartmentNoticeInfoJDBCRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@Transactional
@EnableScheduling
@RequiredArgsConstructor
public class ScrapService {

    private final DepartmentService departmentService;

    private final NotificationService notificationService;

    private final DepartmentNoticeInfoJDBCRepository departmentNoticeInfoJDBCRepository;

    private final CollectedNotificationsJDBCRepository collectedNotificationsJDBCRepository;

    private final SlackService slackService;

    @Value("${mycustom.collection-date-range:2}")
    private int collectionDateRange;

    private boolean sqsStatus = false; // 기본값은 false

    private final AwsSqsSender awsSqsSender;

    // scrap, 요청 관계 없이 스케줄링으로 처리
    @Scheduled(cron = "${spring.task.scheduling.cron}")
    public void scrap() {

        log.info("[SCRAP-Scheduled START]");

        // 스크랩 결과 저장 자료구조 추가
        List<ScrapResultDto> resultList = new ArrayList<>();

        List<ScrapFailedDto> failedList = new ArrayList<>();

        String baseUrl = "https://www.gnu.ac.kr/%s/na/ntt/";

        // 모든 Department 와 DepartmentNoticeInfo (세부 정보) 획득
        List<Department> allDptList = departmentService.getAllDepartmentNoticeInfo();

        // lastNttSn 상태저장 List
        List<LastNttsnDto> lastNttsnDtoList = new ArrayList<>();

        // 모든 Department 에 대해서 반복작업 수행
        for (Department department : allDptList) {

            // 부서단위 스크랩 결과 저장 객체(부서(부서상세정보1, 부서상세정보2, ..))
            ScrapResultDto scrapResultDto = ScrapResultDto.builder()
                    .departmentId(department.getDepartmentId())
                    .departmentName(department.getDepartmentKo())
                    .scrapResultList(new ArrayList<>())
                    .build();


            List<DepartmentNoticeInfo> departmentNoticeInfoList = department.getDepartmentNoticeInfoList();
            String departmentKo = department.getDepartmentKo(); // 부서 이름
            String formattedBaseUrl = String.format(baseUrl, department.getDepartmentEng());

            // formattedBaseUrl, mi, bbsid = 1 + 3
            String baseNoticeUrl = "%sselectNttList.do?mi=%s&bbsId=%s";

            // formattedBaseUrl, mi, bbsid, nttsn = 1 + 4
            String baseNoticeLinkUrl = "%sselectNttInfo.do?mi=%s&bbsId=%s&nttSn=%s";

            // 부서 별 다수 공지사항 링크가 존재
            for (DepartmentNoticeInfo departmentNoticeInfo : departmentNoticeInfoList) {
                TreeSet<Integer> nttSnSet = new TreeSet<>(Comparator.reverseOrder());

                int mi = departmentNoticeInfo.getMi(); // 공지사항 접속 링크 - queryParam: mi
                int bbsId = departmentNoticeInfo.getBbsId(); // 공지사항 접속 링크 - queryParam: bbs_id
                String formattedNoticeUrl = String.format(baseNoticeUrl, formattedBaseUrl, mi, bbsId);
                log.debug("[{}]={}", departmentKo, formattedNoticeUrl);

                try { // Jsoup
                    Document document = Jsoup.connect(formattedNoticeUrl).get();
                    Element thead = document.selectFirst("thead");
                    Element tbody = document.selectFirst("tbody");

                    // notice headers 찾기
                    Elements noticeHeaders = thead.select("th");
                    // 새로운 공지 HTML 행 찾기
                    Elements newNoticeHtmls = tbody.select("tr");
                    //log.debug("noticeHeaders={}", noticeHeaders.toString());
                    for (Element noticeHeader : noticeHeaders) {
                        noticeHeader.text(noticeHeader.text().trim());
                    }
                    // 날짜 인덱스
                    int dateIdx = -1;
                    for (int i = 0; i < noticeHeaders.size(); i++) { // 등록일 탐색
                        String headerText = noticeHeaders.get(i).text().trim();
                        if (headerText.equals("등록일")) {
                            dateIdx = i;
                            break;
                        }
                    }

                    // 각 공지 링크 가져오기
                    for (Element newNoticeHtml : newNoticeHtmls) {
                        Element element = newNoticeHtml.selectFirst("a.nttInfoBtn");
                        if (element == null) break; // 공지사항이 없을 경우 중단
                        String nttSn = element.attr("data-id");
                        // log.debug("newNoticeHtml={}", newNoticeHtml);

                        // nttSn 확인
                        if (Integer.parseInt(nttSn) <= departmentNoticeInfo.getLastNttSn()) continue;

                        // 날짜 확인
                        String date = newNoticeHtml.select("td").get(dateIdx).text().strip();
                        LocalDate parseNoticeLocalDate = getLocalDate(date);
                        LocalDate now = LocalDate.now();

                        // n일이 지난 공지사항은 제외
                        if (ChronoUnit.DAYS.between(parseNoticeLocalDate, now) > collectionDateRange) continue;

                        String title = newNoticeHtml.select("a").first().ownText().trim();
                        String formattedNoticeLinkUrl = String.format(baseNoticeLinkUrl, formattedBaseUrl, mi, bbsId, nttSn);
                        log.debug("[{} {}]={}", title, date, formattedNoticeLinkUrl);

                        nttSnSet.add(Integer.parseInt(nttSn)); // nttSn 저장

                        // 위의 분기문을 전부 통과한 요소는 새로 등록된 요소
                        ScrapResult scrapResult = ScrapResult.builder()
                                .departmentId(department.getDepartmentId())
                                .departmentName(department.getDepartmentKo())
                                .title(title)
                                .date(date)
                                .noticeLink(formattedNoticeLinkUrl)
                                .build();

                        // sqs check, sqs가 정상 상태라면 scrapResultDto 에 scrapResult 가 들어가지 않음
                        if (sqsStatus) {
                            // SQS 에 전송 시도
                            ScrapNotification sqsMessage = ScrapNotification.of(scrapResult, Topic.DEPT_TOPIC_PATH.getPath() + department.getDepartmentId());
                            boolean result = awsSqsSender.sendScrapNotification(sqsMessage);

                            // result == false -> 메세지 전송 실패, 로컬 메세지 전송 함수를 이용하기 위해 기존 리스트에 추가
                            if (!result) scrapResultDto.getScrapResultList().add(scrapResult);
                        } else {
                            // else
                            scrapResultDto.getScrapResultList().add(scrapResult);
                        }
                    }
                    //nttSnList.sort(Collections.reverseOrder()); // lastNttSn 저장
                    if (!nttSnSet.isEmpty()) { // 공지사항이 스크랩된 경우 == Set 이 비어있지 않은 경우
                        log.debug(nttSnSet.toString());

                        LastNttsnDto lastNttsnDto = LastNttsnDto.builder()
                                .id(departmentNoticeInfo.getNoticeInfoId())
                                .nttSn(nttSnSet.first())
                                .build();

                        // nttSnSet.first() + dto 생성, 리스트에 추가
                        lastNttsnDtoList.add(lastNttsnDto);
                        //departmentNoticeInfo.setLastNttSn(nttSnSet.first());
                    }
                } catch (IOException e) {
                    log.error("[SCRAP IOException] department={}, dept_url={}", department.getDepartmentKo(), formattedNoticeUrl);

                    ScrapFailedDto scrapFailedDto = getScrapFailedDto(department, departmentKo, mi, bbsId, formattedNoticeUrl, "I/O Exception");
                    failedList.add(scrapFailedDto);
                } catch (Exception e) {
                    log.error("[SCRAP Exception] cause={}, department={}, dept_url={}", e.getMessage(), department.getDepartmentKo(), formattedNoticeUrl);

                    ScrapFailedDto scrapFailedDto = getScrapFailedDto(department, departmentKo, mi, bbsId, formattedNoticeUrl, "Exception");
                    failedList.add(scrapFailedDto);
                }

            }
            // 부서 별 세부 크롤링 결과 저장, 저장되어 있는 경우에
            if (!scrapResultDto.getScrapResultList().isEmpty()) {
                resultList.add(scrapResultDto);
            }

        }

        // 알림 발송 함수 호출
        // 무조건 호출되는게 아니라 스크랩된 것이 있으면 호출
        if (!resultList.isEmpty()) {
            notificationService
                    .handleNotificationProcessWithTopic(resultList);
        }

        // batch update
        if (!lastNttsnDtoList.isEmpty()) {
            departmentNoticeInfoJDBCRepository.batchUpdateDeptInfo(lastNttsnDtoList);
        }

        // batch insert
        if (!resultList.isEmpty()) {
            collectedNotificationsJDBCRepository.batchInsert(resultList);
        }

        // SLACK 으로 결과 전송
        if (!resultList.isEmpty() || !failedList.isEmpty()) {
            log.debug("[CALL SLACK MESSAGING], resultList.length={}, failedList.length={}", resultList.size(), failedList.size());
            slackService.sendScrapResultMessage(resultList, failedList);
        }
    }

    @NotNull
    private static ScrapFailedDto getScrapFailedDto(Department department, String departmentKo, int mi, int bbsId, String formattedNoticeUrl, String reason) {
        return new ScrapFailedDto(departmentKo, department.getDepartmentEng(), mi, bbsId, formattedNoticeUrl, reason);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ScrapFailedDto {

        private String departmentKo;

        private String departmentEng;

        private int mi;

        private int bbsId;

        private String formattedNoticeLinkUrl;

        private String reason;
    }

    private static LocalDate getLocalDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        return LocalDate.parse(date, formatter);
    }

    public void setSqsStatus(boolean status) {
        this.sqsStatus = status;
        log.info("[Status Update] sqsStatus={}", sqsStatus);
        slackService.sendSimpleTextMessage("AWS SQS-" + awsSqsSender.getSimpleServiceQueueName() + " 상태가 업데이트 되었습니다. \n현재 SQS-AlertServer status flag=" + this.sqsStatus + "\n알림 작업 서버 상태: " + (this.sqsStatus ? "이용 가능" : "이용 불가"), "알림 작업 서버 신호 수신", this.sqsStatus);
    }
}
