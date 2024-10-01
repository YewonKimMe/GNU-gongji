package site.gnu_gongji.GnuGongji.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import site.gnu_gongji.GnuGongji.entity.Department;
import site.gnu_gongji.GnuGongji.entity.DepartmentNoticeInfo;
import site.gnu_gongji.GnuGongji.exception.ExcelFileAdditionException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequiredArgsConstructor
@Service
public class NoticeExcelParser {

    private final DepartmentService departmentService;

    Map<String, Department> map = new HashMap<>();

    public void readDepartmentInfoExcel(MultipartFile file) {

        if (!StringUtils.getFilenameExtension(file.getOriginalFilename()).equals("xlsx")) { // 확장자가 xlsx 이 아닌 경우
            throw new IllegalArgumentException("잘못된 파일 확장자 입니다.");
        }
        if (departmentService.checkDepartmentExist()) { // 이미 부서 정보가 존재할 경우
            throw new ExcelFileAdditionException("데이터베이스 내에 이미 관련 데이터가 존재하여 저장할 수 없습니다. 개별 추가를 이용해 주세요.");
        }
        try {
            InputStream inputStream = file.getInputStream(); // 입력 스트림 획득
            Workbook workbook = WorkbookFactory.create(inputStream); // 워크북 획득
            Sheet sheet = workbook.getSheetAt(0); // 시트 획득

            int numberOfRows = sheet.getPhysicalNumberOfRows(); // 행 갯수
            String beforeDpName = null;
            for (int r=0; r<numberOfRows; r++) { // 행 갯수-1 만큼 반복
                if (r == 0) { // 첫줄은 버림
                    continue;
                }
                Row row = sheet.getRow(r); // 행 획득
                int numberOfColumns = row.getLastCellNum();
                //log.debug("columns={}", numberOfColumns);
                if (numberOfColumns != 5) {
                    throw new IllegalArgumentException("잘못된 양식의 엑셀 파일 입니다. column: " + numberOfColumns);
                }
                DepartmentNoticeInfo deptNoticeInfo = new DepartmentNoticeInfo();
                deptNoticeInfo.setLastNttSn(0);

                for (int c=0; c<numberOfColumns + 1; c++) {
                    //log.debug("c number={}", c);

                    Cell cell = row.getCell(c); // 셀 획득
                    if (cell == null) continue;

                    String cellValue = cell.toString();
                    boolean isNumber = false;
                    if (cell.getCellType() == CellType.NUMERIC) { // 숫자 형식일 경우 정수로 변환
                        cellValue = String.valueOf((int) cell.getNumericCellValue());
                        isNumber = true;
                    }
                    if (c == 0 && !isNumber && !map.containsKey(cellValue)) { // 0번째 셀(부서명) 이면서 키가 없다면
                        if (!map.isEmpty()) { // 맵이 비어있지 않다면
                            List<DepartmentNoticeInfo> departmentNoticeInfoList = map.get(beforeDpName).getDepartmentNoticeInfoList();

                            //departmentNoticeInfoList.remove(departmentNoticeInfoList.size() - 1);
                            //log.debug("[DP_INFO]={}", deptNoticeInfo);

                            log.debug("[DP]={}", map.get(beforeDpName));
                            departmentService.saveDepartmentComb(map.get(beforeDpName));
                            map.remove(beforeDpName);
                        }
                        if (cellValue.equals("end")){
                            log.debug("cellValue={}", cellValue);
                            return;
                        }
                        Department dp = new Department();
                        dp.setDepartmentNoticeInfoList(new ArrayList<>());
                        map.put(cellValue, dp);
                        beforeDpName = cellValue;
                    }
                    if (!StringUtils.hasText(cellValue) && c == 0) { // 값이 없으면서 0번째 셀이면 넘어가기
                        continue;
                    }
                    //log.debug("cellValue={}",cellValue);

                    switch (c) {
                        // 학과명(ko) -> department 에 세팅
                        case 0 -> {map.get(beforeDpName).setDepartmentKo(cellValue);}

                        // 공지종류 (참고용)
                         case 1 -> {}

                        // 학과명 dept_eng -> department 에 세팅
                        case 2 -> {map.get(beforeDpName).setDepartmentEng(cellValue);}

                        // mi
                        case 3 -> deptNoticeInfo.setMi(Integer.parseInt(cellValue));

                        // bbs_id
                        case 4 -> deptNoticeInfo.setBbsId(Integer.parseInt(cellValue));

                        default -> throw new RuntimeException("올바르지 않은 셀 번호");
                    }
                }
                deptNoticeInfo.setDepartment(map.get(beforeDpName));
                map.get(beforeDpName).getDepartmentNoticeInfoList().add(deptNoticeInfo);

            }
        } catch (FileNotFoundException e) {
            log.debug("File 을 찾을 수 없음");
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.debug("IO 예외");
            throw new RuntimeException(e);
        }
        map.clear();
    }
}