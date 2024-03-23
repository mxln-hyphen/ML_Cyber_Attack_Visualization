package Innercommon.Enum;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum HTTPMethod {
    NOT_HTTP,
    GET,
    POST
    ;


    public static HTTPMethod codeOf(int code) {
        for (HTTPMethod prizes : values()) {
            if (prizes.ordinal() == code) {
                return prizes;
            }
        }
        log.error("没有对应枚举类");
        return null;
    }
}
