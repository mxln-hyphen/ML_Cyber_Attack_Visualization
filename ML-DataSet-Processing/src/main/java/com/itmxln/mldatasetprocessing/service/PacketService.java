package com.itmxln.mldatasetprocessing.service;

import Innercommon.Enum.ConnectionState;
import Innercommon.Enum.NewConnectionState;
import org.pcap4j.packet.*;
import org.springframework.stereotype.Service;

@Service
public class PacketService {

    /**
     * 判断数据包是不是一个连接的开始
     *
     * @return
     */
    public boolean isStart(Packet packet) {
        //获取标志位信息
        TcpPacket tcpPacket = packet.get(TcpPacket.class);
        TcpPacket.TcpHeader header = tcpPacket.getHeader();
        boolean ack = header.getAck();
        boolean syn = header.getSyn();

        //如果有syn且没有ack
        return syn && !ack;
    }

    /**
     * 根据数据包确定连接的状态
     */
    public NewConnectionState getState(Packet packet, NewConnectionState presentState) {
        //获取标志位信息
        TcpPacket tcpPacket = packet.get(TcpPacket.class);
        TcpPacket.TcpHeader header = tcpPacket.getHeader();
        boolean ack = header.getAck();
        boolean syn = header.getSyn();
        boolean fin = header.getFin();
        boolean rst = header.getRst();

        //如果是RST，中断连接
        if (rst)
            return NewConnectionState.RST_FINISHED;

        // 根据捕获到的TCP包的标志位更新连接状态
        switch (presentState) {
            case CLOSED:
                // 连接初始状态，等待发送SYN包或接收SYN包
                if (syn && !ack) {
                    return NewConnectionState.SYN_SENT;
                }
                break;
            case SYN_SENT:
                // 已发送SYN包，等待SYN-ACK响应
                if (syn && ack) {
                    return NewConnectionState.SYN_RECEIVED;
                }
                break;
            case SYN_RECEIVED:
                // 收到SYN包并发送了SYN-ACK包，等待ACK确认连接建立
                if (!syn && ack) {
                    return NewConnectionState.ESTABLISHED;
                }
                break;
            case ESTABLISHED:
                // 连接已建立，可以进行数据通信
                // 如果发送或接收到FIN包，开始关闭连接的过程
                if (fin) {
                    return NewConnectionState.FIN_WAIT_1;
                }
                break;
            case FIN_WAIT_1:
                if (fin) {//因为四次挥手时，服务端可能会将两个数据包（ACK,FIN+ACK）合并，所以我们检查一下这个包是否带FIN
                    return NewConnectionState.FINISHED;
                }
                // 已发送FIN包，等待ACK响应
                if (ack) {
                    return NewConnectionState.FIN_WAIT_2;
                }
                break;
            case FIN_WAIT_2:
                // 收到对方的ACK响应FIN包，等待对方的FIN包
                if (fin && ack) {
                    return NewConnectionState.FINISHED;
                }
        }

        //状态不需要变
        return presentState;
    }

    /**
     * 获取源ip
     *
     * @return
     */
    public String getSourceIp(Packet packet) {
        IpPacket ipPacket = packet.get(IpPacket.class);
        if (ipPacket instanceof IpV4Packet) {
            return ((IpV4Packet) ipPacket).getHeader().getSrcAddr().toString().substring(1);
        } else if (ipPacket instanceof IpV6Packet) {
            return ((IpV6Packet) ipPacket).getHeader().getSrcAddr().toString().substring(1);
        }
        return "";
    }

    /**
     * 获取目的ip
     *
     * @return
     */
    public String getDstIp(Packet packet) {
        IpPacket ipPacket = packet.get(IpPacket.class);
        if (ipPacket instanceof IpV4Packet) {
            return ((IpV4Packet) ipPacket).getHeader().getDstAddr().toString().substring(1);
        } else if (ipPacket instanceof IpV6Packet) {
            return ((IpV6Packet) ipPacket).getHeader().getDstAddr().toString().substring(1);
        }
        return "";
    }

    /**
     * 获取源端口
     *
     * @return
     */
    public int getSourcePort(Packet packet) {
        TcpPacket tcpPacket = packet.get(TcpPacket.class);

        return tcpPacket.getHeader().getSrcPort().valueAsInt();
    }

    /**
     * 获取目的端口
     *
     * @return
     */
    public int getDstPort(Packet packet) {
        TcpPacket tcpPacket = packet.get(TcpPacket.class);

        return tcpPacket.getHeader().getDstPort().valueAsInt();
    }
}