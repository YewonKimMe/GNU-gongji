package site.gnu_gongji.GnuGongji.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import site.gnu_gongji.GnuGongji.dto.ScrapResult;
import site.gnu_gongji.GnuGongji.dto.ScrapResultDto;
import site.gnu_gongji.GnuGongji.tool.UUIDConverter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Repository
public class CollectedNotificationsJDBCRepository {

    private final JdbcTemplate jdbcTemplate;

    public void batchInsert(List<ScrapResultDto> scrapResultDtoList) {

        List<ScrapResult> scrapResults = new ArrayList<>();

        for (ScrapResultDto scrapResultDto : scrapResultDtoList) {
            Collections.reverse(scrapResultDto.getScrapResultList());
            scrapResults.addAll(scrapResultDto.getScrapResultList());
        }

        String sql = "INSERT INTO collected_notification (department_id, department_name, noti_title, date_time, link, created_time, uuid) VALUES (?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                if ( i == 0) {
                    log.debug("size={}", scrapResults.size());
                }

                ScrapResult find = scrapResults.get(i);

                ps.setLong(1, find.getDepartmentId());
                ps.setString(2, find.getDepartmentName());
                ps.setString(3, find.getTitle());
                ps.setString(4, find.getDate());
                ps.setString(5, find.getNoticeLink());
                ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                ps.setBytes(7, UUIDConverter.convertUuidStringToBinary16(find.getUuid()));
            }

            @Override
            public int getBatchSize() {
                return scrapResults.size();
            }
        });

    }
}
