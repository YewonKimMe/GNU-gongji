package site.gnu_gongji.GnuGongji.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.gnu_gongji.GnuGongji.dto.DepartmentDto;
import site.gnu_gongji.GnuGongji.entity.Department;
import site.gnu_gongji.GnuGongji.repository.DepartmentRepository;
import site.gnu_gongji.GnuGongji.enums.RedisConst;

import java.util.List;

@Transactional
@RequiredArgsConstructor
@Slf4j
@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    private final RedisService redisService;

    public void saveDepartmentComb(Department department) {

        departmentRepository.saveDepartment(department);
    }

    public boolean checkDepartmentExist() {
        return departmentRepository.checkDepartmentExist();
    }

    public List<Department> getAllDepartmentNoticeInfo() {
        return departmentRepository.getAllDepartmentNoticeInfo();
    }

    public List<DepartmentDto> getAllDepartment() {
        // redis 에서 key 로 조회 시도
        List<DepartmentDto> redisFindList = redisService.getAllListItems(RedisConst.DEPARTMENT_DTO.getValue(), DepartmentDto.class);

        log.debug("redisFindList={}", redisFindList);

        // redis 에 캐싱된 데이터가 없으면 저장 후 리턴
        if (redisFindList == null || redisFindList.isEmpty()) {
            log.debug("redisFindList is empty");
            List<DepartmentDto> allDepartments = departmentRepository.getAllDepartment();
            redisService.saveList(RedisConst.DEPARTMENT_DTO.getValue(), allDepartments);
            return allDepartments;
        }

        return redisFindList;
    }

    public boolean checkDepartmentExistByDId(Long departmentId) {
        return departmentRepository.checkDepartmentByDId(departmentId);
    }
}
