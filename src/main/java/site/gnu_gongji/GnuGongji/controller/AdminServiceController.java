package site.gnu_gongji.GnuGongji.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import site.gnu_gongji.GnuGongji.dto.response.ResultAndMessage;
import site.gnu_gongji.GnuGongji.dto.response.SuccessResultAndMessage;
import site.gnu_gongji.GnuGongji.service.NoticeExcelParser;

@RequiredArgsConstructor
@Slf4j
@RestController
@Tag(name = "Admin", description = "관리자 기능 관련 엔드포인트")
@RequestMapping("/api/v1/admin")
public class AdminServiceController {

    private final NoticeExcelParser noticeExcelParser;

    @Operation(summary = "부서 엑셀 파일 등록", description = "부서 엑셀 파일을 등록하는 API")
    @PostMapping(value = "/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResultAndMessage<String>> insertNoticeExcel(@Parameter(
            description = "학과 엑셀 파일, 양식 확인 필수",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
    )@RequestPart("multipartFile") MultipartFile file) {

        noticeExcelParser.readDepartmentInfoExcel(file);
        return ResponseEntity.ok()
                .body(new SuccessResultAndMessage<>(HttpStatus.OK.getReasonPhrase(), "업로드 완료"));
    }
}
