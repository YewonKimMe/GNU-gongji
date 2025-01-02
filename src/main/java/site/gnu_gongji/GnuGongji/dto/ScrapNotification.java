package site.gnu_gongji.GnuGongji.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class ScrapNotification {

    private ScrapResult scrapResult;

    private String firebaseTopic;

    private LocalDateTime createdAt;

    private String messageId;

    public ScrapNotification () {

    }

    public static ScrapNotification of(ScrapResult scrapResult, String firebaseTopic) {
        return new ScrapNotification(scrapResult, firebaseTopic, LocalDateTime.now(), UUID.randomUUID().toString());
    }
}
