package site.gnu_gongji.GnuGongji.exception;

public class SubscriptionLimitReachedException extends RuntimeException {
    public SubscriptionLimitReachedException(String message) {
        super(message);
    }
}
