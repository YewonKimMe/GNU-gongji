package site.gnu_gongji.GnuGongji.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.gnu_gongji.GnuGongji.dto.FcmNotificationDto;
import site.gnu_gongji.GnuGongji.dto.response.ResultAndMessage;
import site.gnu_gongji.GnuGongji.dto.response.SuccessResultAndMessage;
import site.gnu_gongji.GnuGongji.entity.Department;
import site.gnu_gongji.GnuGongji.service.DepartmentService;
import site.gnu_gongji.GnuGongji.service.FcmService;
import site.gnu_gongji.GnuGongji.service.NoticeExcelParser;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@RestController
@Tag(name = "Admin", description = "관리자 기능 관련 엔드포인트")
@RequestMapping("/api/v1/admin")
public class AdminServiceController {

    private final NoticeExcelParser noticeExcelParser;

    private final DepartmentService departmentService;

    private final FcmService fcmService;

    @Operation(summary = "부서 엑셀 파일 등록", description = "부서 엑셀 파일을 등록하는 API")
    @PostMapping(value = "/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResultAndMessage> insertNoticeExcel(@Parameter(
            description = "학과 엑셀 파일, 양식 확인 필수",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
    )@RequestPart("multipartFile") MultipartFile file) {

        noticeExcelParser.readDepartmentInfoExcel(file);
        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), "업로드 완료"));
    }

    @GetMapping("/all-dept")
    public ResponseEntity<List<Department>> getAllDpt() {
        List<Department> allDepartmentNoticeInfo = departmentService.getAllDepartmentNoticeInfo();

        return ResponseEntity.ok()
                .body(allDepartmentNoticeInfo);
    }

    @PostMapping("/firebase-notification-test")
    public ResponseEntity<ResultAndMessage> sendNotification(@RequestBody FcmNotificationDto fcmNotificationDto) {

        fcmNotificationDto.setUuid(UUID.randomUUID().toString());

        fcmService.sendMessage(fcmNotificationDto, false);

        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), "전송완료"));

    }
}
