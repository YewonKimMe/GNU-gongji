package site.gnu_gongji.GnuGongji.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import site.gnu_gongji.GnuGongji.dto.LastNttsnDto;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Repository
public class DepartmentNoticeInfoJDBCRepository {

    private final JdbcTemplate jdbcTemplate;

    public void batchUpdateDeptInfo(List<LastNttsnDto> lastNttsnDtoList) {

        String sql = "UPDATE department_notice_info SET last_ntt_sn = ? WHERE notice_info_id = ?";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {

                if (i == 0) {
                    log.debug("size={}", lastNttsnDtoList.size());
                }

                LastNttsnDto lastNttsnDto = lastNttsnDtoList.get(i);
                ps.setInt(1, lastNttsnDto.getNttSn());
                ps.setLong(2, lastNttsnDto.getId());
            }

            @Override
            public int getBatchSize() {
                return lastNttsnDtoList.size();
            }
        });
    }
}
