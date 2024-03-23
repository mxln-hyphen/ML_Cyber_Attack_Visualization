package Innercommon.Enum;

public enum ConnectionState {
    CLOSED, // 连接已关闭或四次挥手完成后的最终状态
    SYN_SENT, // 已发送SYN包，等待SYN-ACK响应
    SYN_RECEIVED, // 收到SYN包并发送了SYN-ACK包，等待ACK
    ESTABLISHED, // 连接已建立，三次握手完成
    FIN_WAIT_1, // 主动关闭连接，已发送FIN包
    FIN_WAIT_2, // 收到对方的ACK响应FIN包，等待对方的FIN包
    TIME_WAIT, // 收到对方的FIN包并发送ACK响应，等待一段时间以确保对方收到ACK
    FINISHED // 连接完成并且成功断开
}