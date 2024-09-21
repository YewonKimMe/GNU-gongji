package site.gnu_gongji.GnuGongji.dto.response;

public record FailResultAndMessage<T>(String httpMessage, T data) implements ResultAndMessage {
}
