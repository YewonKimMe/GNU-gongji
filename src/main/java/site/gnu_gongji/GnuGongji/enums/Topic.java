package site.gnu_gongji.GnuGongji.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// TOPIC 규칙: TOPIC_PATH + id
@RequiredArgsConstructor
@Getter
public enum Topic {

    DEPT_TOPIC_PATH("topic_department_"),

    ONLY_TEST_TOPIC_PATH("topic_only_test");

    private final String path;
}
