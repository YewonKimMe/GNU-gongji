package site.gnu_gongji.GnuGongji.repository;

import site.gnu_gongji.GnuGongji.entity.Department;

import java.util.List;

public interface DepartmentRepository {

    void saveDepartment(Department department);

    boolean checkDepartmentExist();

    boolean checkDepartmentByDId(Long departmentId);

    List<Department> getAllDepartmentNoticeInfo();
}
