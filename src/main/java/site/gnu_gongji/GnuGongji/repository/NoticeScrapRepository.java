package site.gnu_gongji.GnuGongji.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import site.gnu_gongji.GnuGongji.entity.*;

@Slf4j
@Repository
public class NoticeScrapRepository {

    private final JPAQueryFactory queryFactory;

    public NoticeScrapRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    // 유저가 구독한 공지사항 세부정보만을 가져오는 리스트
    // 유저 이메일, dept_eng, dept_ko, mi, notice_info_id, bbsid, ntt_sn 가져오기.
    public Object getNoticeInfoWhatUserSubscribed() {
        QUser user = QUser.user;
        QUserSub userSub = QUserSub.userSub;
        QUserSubDepartment userSubDepartment = QUserSubDepartment.userSubDepartment; // 중간 테이블
        QDepartment department = QDepartment.department;
        QDepartmentNoticeInfo departmentNoticeInfo = QDepartmentNoticeInfo.departmentNoticeInfo;


        return null;
    }

}
