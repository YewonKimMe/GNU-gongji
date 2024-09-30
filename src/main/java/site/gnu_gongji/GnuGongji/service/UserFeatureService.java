package site.gnu_gongji.GnuGongji.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.gnu_gongji.GnuGongji.dto.DepartmentDto;
import site.gnu_gongji.GnuGongji.dto.EmailDto;
import site.gnu_gongji.GnuGongji.entity.User;
import site.gnu_gongji.GnuGongji.entity.UserSub;
import site.gnu_gongji.GnuGongji.exception.*;
import site.gnu_gongji.GnuGongji.repository.UserFeatureRepository;

import java.util.List;
import java.util.Optional;

@Transactional
@RequiredArgsConstructor
@Slf4j
@Service
public class UserFeatureService {

    private final DepartmentService departmentService;

    private final UserFeatureRepository userFeatureRepository;

    // 이메일 변경
    public void updateUserEmail(final String userOAuth2Id, final EmailDto emailDto) {

        User findUser = userFeatureRepository.findUserByOauth2Id(userOAuth2Id)
                .orElseThrow(() -> new UserNotExistException("해당 ID로 조회된 유저가 없습니다."));

        findUser.setUserEmail(emailDto.getEmail());
    }

    // 유저 공지사항 구독 추가
    public void addUserSubDepartment(final String userOAuth2Id, final Long departmentId) {

        // 유효한 부서 여부 확인
        checkDepartment(departmentId);

        // 유저 조회
        Optional<User> findUserOpt = userFeatureRepository.findUserByOauth2Id(userOAuth2Id);

        if (findUserOpt.isEmpty()) {
            throw new UserNotExistException("해당 ID로 조회된 유저가 없습니다.");
        }

        User findUser = findUserOpt.get();

        int userSubSize = findUser.getSubList().size();

        if (userSubSize >= 3) {
            throw new SubscriptionLimitReachedException("공지사항 구독은 최대 2개 까지만 가능합니다.\n현재 구독중인 갯수는 " + userSubSize + " 입니다.");
        }

        // userSub 에 해당 deptId 존재 여부 체크(중복 방지)
        findUser.getSubList()
                .stream()
                .filter(userSub -> userSub.getDepartmentId().equals(departmentId))
                .findFirst()
                .ifPresent(sub -> {throw new DuplicateSubscribeException("이미 구독중인 학과/부서입니다.");
                });

        UserSub userSub = new UserSub();

        userSub.setDepartmentId(departmentId);
        userSub.setUser(findUser);

        findUser.getSubList().add(userSub);
    }

    // 구독중인 유저 공지사항 삭제
    public void deleteSubscription(final String userOAuth2Id, final Long departmentId) {

        // 유효한 여부 확인
        checkDepartment(departmentId);

        // 유저 조회
        Optional<User> findUserOpt = userFeatureRepository.findUserByOauth2Id(userOAuth2Id);

        // 조회 결과가 없는 경우
        if (findUserOpt.isEmpty()) {
            throw new UserNotExistException("해당 ID로 조회된 유저가 없습니다.");
        }

        // OAuth2Id 로 조회된 유저
        User findUser = findUserOpt.get();

        UserSub findUserSub = findUserOpt.get().getSubList()
                .stream()
                .filter(userSub -> userSub.getDepartmentId().equals(departmentId))
                .findFirst()
                .orElseThrow(() -> new NotExistKeyException("해당 ID로 조회된 구독 정보가 없습니다."));

        // UserSubList 에서 해당 userSub 제거
        findUser.getSubList().remove(findUserSub);

        // 양방향 관계 정리
        findUserSub.setUser(null);
    }

    private void checkDepartment(Long departmentId) {
        // Department 조회
        if (!departmentService.checkDepartmentExistByDId(departmentId)) {
            throw new DepartmentNotExistException("해당 ID로 검색된 학과/부서가 없습니다.");
        }
    }

    public List<DepartmentDto> getUserSub(String oauth2Id) {
        return userFeatureRepository.findDepartmentsByUserOauth2Id(oauth2Id);
    }
}
