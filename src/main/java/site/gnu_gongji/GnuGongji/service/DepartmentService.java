package site.gnu_gongji.GnuGongji.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.gnu_gongji.GnuGongji.entity.Department;
import site.gnu_gongji.GnuGongji.repository.DepartmentRepository;

import java.util.List;

@Transactional
@RequiredArgsConstructor
@Slf4j
@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public void saveDepartmentComb(Department department) {

        departmentRepository.saveDepartment(department);
    }

    public boolean checkDepartmentExist() {
        return departmentRepository.checkDepartmentExist();
    }

    public List<Department> getAllDepartmentNoticeInfo() {
        return departmentRepository.getAllDepartmentNoticeInfo();
    }

    public boolean checkDepartmentExistByDId(Long departmentId) {
        return departmentRepository.checkDepartmentByDId(departmentId);
    }
}
