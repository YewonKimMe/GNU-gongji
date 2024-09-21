package site.gnu_gongji.GnuGongji.dto.response;

public record SuccessResultAndMessage<T>(String httpMessage, T data) implements ResultAndMessage {

}
