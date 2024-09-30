package site.gnu_gongji.GnuGongji.repository;


import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import site.gnu_gongji.GnuGongji.dto.DepartmentDto;
import site.gnu_gongji.GnuGongji.entity.Department;

import java.util.List;

@Slf4j
@Repository
public class DepartmentRepositoryImpl implements DepartmentRepository {

    private final EntityManager em;

    private final JPAQueryFactory queryFactory;

    public DepartmentRepositoryImpl(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public void saveDepartment(Department department) {
        em.persist(department);
    }

    @Override
    public boolean checkDepartmentExist() {
        String jpql = "SELECT COUNT(e) FROM Department e";
        Long count = em.createQuery(jpql, Long.class).getSingleResult();
        return count > 0;
    }

    @Override
    public boolean checkDepartmentByDId(final Long departmentId) {

        String jpql = "SELECT COUNT(e) FROM Department e WHERE e.departmentId = :departmentId";
        Long count = em.createQuery(jpql, Long.class)
                .setParameter("departmentId", departmentId).getSingleResult();
        return count > 0;
    }

    @Override
    public List<Department> getAllDepartmentNoticeInfo() {

        return em.createQuery("SELECT d FROM Department d JOIN FETCH d.departmentNoticeInfoList", Department.class)
                .getResultList();
    }

    @Override
    public List<DepartmentDto> getAllDepartment() {
        return em.createQuery("SELECT new site.gnu_gongji.GnuGongji.dto.DepartmentDto(d.departmentId, d.departmentKo, d.departmentEng) FROM Department d")
                .getResultList();
    }
}
