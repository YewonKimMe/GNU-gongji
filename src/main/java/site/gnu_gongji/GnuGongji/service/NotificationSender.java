package site.gnu_gongji.GnuGongji.service;

import site.gnu_gongji.GnuGongji.dto.ScrapNotification;

public interface NotificationSender {

    boolean sendScrapNotification(ScrapNotification scrapNotification);
}
