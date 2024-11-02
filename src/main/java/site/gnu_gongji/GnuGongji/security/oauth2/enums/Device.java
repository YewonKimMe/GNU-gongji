package site.gnu_gongji.GnuGongji.security.oauth2.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Device {

    MOBILE("MOBILE"),
    TABLET("TABLET"),
    PC("PC");

    private final String device;

    public static Device getDevice(String value) {
        if (value == null) return null;
        for (Device device : values()) {
            if (device.getDevice().equals(value)) {
                return device;
            }
        }
        throw new RuntimeException("Device Not Match");
    }
}
