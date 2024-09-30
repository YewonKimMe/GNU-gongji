package site.gnu_gongji.GnuGongji.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.gnu_gongji.GnuGongji.dto.DepartmentDto;
import site.gnu_gongji.GnuGongji.dto.response.ResultAndMessage;
import site.gnu_gongji.GnuGongji.dto.response.SuccessResultAndMessage;
import site.gnu_gongji.GnuGongji.service.DepartmentService;

import java.util.List;

@Slf4j
@RestController
@Tag(name = "Info", description = "정보 관련 엔드포인트")
@RequiredArgsConstructor
@RequestMapping("/api/v1/info")
public class InfoController {

    private final DepartmentService departmentService;

    // 구독 가능 학과 리스트 획득
    @Operation(summary = "구독 가능 학과 리스트", description = "구독 가능 학과 리스트 획득 API")
    @GetMapping("/departments")
    public ResponseEntity<ResultAndMessage> getDepartmentList() {
        List<DepartmentDto> result = departmentService.getAllDepartment();
        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage(HttpStatus.OK.getReasonPhrase(), result));
    }

    // 학과 또는 단과대? 검색 기능
}
