package site.gnu_gongji.GnuGongji.security.oauth2.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// TOPIC 규칙: TOPIC_PATH + id
@RequiredArgsConstructor
@Getter
public enum Topic {

    DEPT_TOPIC_PATH("topic_department_");

    private final String path;
}
