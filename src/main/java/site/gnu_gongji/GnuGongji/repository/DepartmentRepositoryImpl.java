package site.gnu_gongji.GnuGongji.repository;


import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import site.gnu_gongji.GnuGongji.entity.Department;

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
}
