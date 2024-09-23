package site.gnu_gongji.GnuGongji.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.gnu_gongji.GnuGongji.entity.Department;
import site.gnu_gongji.GnuGongji.entity.DepartmentNoticeInfo;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ScrapService {

    private final DepartmentService departmentService;

    public void scrap() {

        String baseUrl = "https://www.gnu.ac.kr/%s/na/ntt/";

        List<Department> allDptList = departmentService.getAllDepartmentNoticeInfo();
        for (Department department : allDptList) {
            List<DepartmentNoticeInfo> departmentNoticeInfoList = department.getDepartmentNoticeInfoList();
            String departmentKo = department.getDepartmentKo();
            String formattedBaseUrl = String.format(baseUrl, department.getDepartmentEng());
            //log.debug("[formattedBaseUrl]={}", formattedBaseUrl);

            // formattedBaseUrl, mi, bbsid = 1 + 3
            String baseNoticeUrl = "%sselectNttList.do?mi=%s&bbsId=%s";
            // formattedBaseUrl, mi, bbsid, nttsn = 1 + 4
            String baseNoticeLinkUrl = "%sselectNttInfo.do?mi=%s&bbsId=%s&nttSn=%s";
            for (DepartmentNoticeInfo departmentNoticeInfo : departmentNoticeInfoList) {
                // 공지사항 접속링크
                int mi = departmentNoticeInfo.getMi();
                int bbsId = departmentNoticeInfo.getBbsId();
                String formattedNoticeUrl = String.format(baseNoticeUrl, formattedBaseUrl, mi, bbsId);
                log.debug("[{}]={}", departmentKo, formattedNoticeUrl);
                try {
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
                    for (int i=0; i<noticeHeaders.size(); i++) {
                        String headerText = noticeHeaders.get(i).text().trim();
                        if (headerText.equals("등록일")) {
                            dateIdx = i;
                            break;
                        }
                    }
                    log.debug("dateIdx={}", dateIdx);
                    // log.debug("newNoticeHtmls={}", newNoticeHtmls.toString());
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

                        // 20일이 지난 공지사항은 제외
                        if (ChronoUnit.DAYS.between(parseNoticeLocalDate, now) > 20) continue;

                        String title = newNoticeHtml.select("a").first().ownText().trim();
                        String formattedNoticeLinkUrl = String.format(baseNoticeLinkUrl, formattedBaseUrl, mi, bbsId, nttSn);
                        log.debug("[{} {}]={}", title, date, formattedNoticeLinkUrl);

                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    private static LocalDate getLocalDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        return LocalDate.parse(date, formatter);
    }
}
