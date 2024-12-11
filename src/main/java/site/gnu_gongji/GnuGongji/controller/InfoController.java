package site.gnu_gongji.GnuGongji.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.gnu_gongji.GnuGongji.dto.DepartmentDto;
import site.gnu_gongji.GnuGongji.dto.response.ResultAndMessage;
import site.gnu_gongji.GnuGongji.dto.response.SuccessResultAndMessage;
import site.gnu_gongji.GnuGongji.entity.CollectedNotifications;
import site.gnu_gongji.GnuGongji.service.DepartmentService;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), result));
    }

    // 학과 또는 단과대? 검색 기능

    @GetMapping("/notifications/{departmentId}")
    public ResponseEntity<PagedModel<EntityModel<CollectedNotifications>>> getCollectedNotification(@PathVariable("departmentId") Long departmentId,
                                                                                           @RequestParam(name = "page", defaultValue = "0") String page,
                                                                                           @RequestParam(name = "size", defaultValue = "50") String size,
                                                                                           @RequestParam(name = "query", required = false) String notificationTitleQuery,
                                                                                           PagedResourcesAssembler<CollectedNotifications> assembler
                                                            ) {
        Integer newPage = Integer.parseInt(page) - 1;
        if (newPage < 0) {
            newPage = 0;
        }
        log.debug("newPage={}", newPage);
        Integer newSize = Integer.parseInt(size);

        Page<CollectedNotifications> find = departmentService.getAllCollectedNotifications(newPage, newSize, departmentId, notificationTitleQuery);

        PagedModel<EntityModel<CollectedNotifications>> model = assembler.toModel(find);

        return ResponseEntity.ok()
                .body(model);
    }
}
