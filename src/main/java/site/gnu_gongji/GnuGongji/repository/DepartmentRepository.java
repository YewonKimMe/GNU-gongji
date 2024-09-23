package site.gnu_gongji.GnuGongji.repository;

import site.gnu_gongji.GnuGongji.entity.Department;

public interface DepartmentRepository {

    void saveDepartment(Department department);

    boolean checkDepartmentExist();
}
