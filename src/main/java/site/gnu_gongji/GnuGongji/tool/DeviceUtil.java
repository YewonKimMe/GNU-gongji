package site.gnu_gongji.GnuGongji.tool;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import site.gnu_gongji.GnuGongji.enums.Device;

@Slf4j
public class DeviceUtil {

    public static Device getDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent").toLowerCase();

        log.debug(userAgent);
        if (userAgent.contains("mobi")) {
            return Device.MOBILE;
        } else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return Device.TABLET;
        } else if (userAgent.contains("windows") || userAgent.contains("macintosh") || userAgent.contains("linux")){
            return Device.PC;
        }
        throw new RuntimeException("Device Not Match");
    }
}
