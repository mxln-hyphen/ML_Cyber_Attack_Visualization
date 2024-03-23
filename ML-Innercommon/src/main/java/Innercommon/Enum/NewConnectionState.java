package Innercommon.Enum;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum NewConnectionState {

    CLOSED, // 连接已关闭或四次挥手完成后的最终状态

    SYN_SENT, // 已发送SYN包，等待SYN-ACK响应

    SYN_RECEIVED, // 收到SYN包并发送了SYN-ACK包，等待ACK

    ESTABLISHED, // 连接已建立，三次握手完成

    FIN_WAIT_1, // 主动关闭连接，已发送FIN包

    FIN_WAIT_2, // 收到对方的ACK响应FIN包，等待对方的FIN包

    FINISHED, // 收到对方的ACK包，连接完成并且成功断开

    RST_FINISHED, // 收到RST包，发生异常，直接中断连接

    TIME_OVER //超时，没有接收到后续的包
    ;

    public static NewConnectionState codeOf(int code) {
        for (NewConnectionState prizes : values()) {
            if (prizes.ordinal() == code) {
                return prizes;
            }
        }
        log.error("没有对应枚举类");
        return null;
    }

}