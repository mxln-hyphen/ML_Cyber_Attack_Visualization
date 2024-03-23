package Innercommon.datastructure;

import lombok.Getter;
import lombok.Setter;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import Innercommon.Enum.ConnectionState;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 记录一次TCP连接
 */
@Getter
@Setter
@Document(indexName = "connections")
public class Connection {
    @Transient
    private List<Packet> packets = new ArrayList<>(); // 该连接中所有的数据包

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String connectionId; //连接标识符

    @Field(type = FieldType.Ip)
    private String sourceIpAddress; // 源IP地址

    @Field(type = FieldType.Ip)
    private String destinationIpAddress; // 目的IP地址

    @Field(type = FieldType.Integer)
    private int sourcePort; // 源端口号

    @Field(type = FieldType.Integer)
    private int destinationPort; // 目的端口号

    @Field(type = FieldType.Keyword)
    private ConnectionState state = ConnectionState.CLOSED; //连接状态

    @Field(type = FieldType.Integer)
    private int connectionDuration; // 连接时长，单位：秒

    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "uuuu-MM-dd'T'HH:mm:ssZZ")
    private Instant firstPacketTimestamp; // 第一个数据包的时间戳

    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "uuuu-MM-dd'T'HH:mm:ssZZ")
    private Instant lastPacketTimestamp; // 最后一个数据包的时间戳

    @Field(type = FieldType.Integer)
    private int packetCount = 0; // 数据包数量

    public Connection(String connectionId,String sourceIpAddress, String destinationIpAddress, int sourcePort, int destinationPort) {
        this.connectionId = connectionId;
        this.sourceIpAddress = sourceIpAddress;
        this.destinationIpAddress = destinationIpAddress;
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
    }
    
    // 添加数据包到连接
    public void addPacket(Packet packet, Instant timestamp) {
        if (packets.isEmpty()) {
            this.firstPacketTimestamp = timestamp;
        }
        this.lastPacketTimestamp = timestamp;
        if (packet.contains(TcpPacket.class)) {
            updateConnectionState(packet.get(TcpPacket.class));
        }
        this.packets.add(packet);
        this.packetCount++; // 更新数据包数量
    }

    /**
     * 更新连接的阶段
     * @param tcpPacket
     */
    private void updateConnectionState(TcpPacket tcpPacket) {
        TcpPacket.TcpHeader header = tcpPacket.getHeader();
        boolean ack = header.getAck();
        boolean fin = header.getFin();
        boolean syn = header.getSyn();

        // 根据捕获到的TCP包的标志位更新连接状态
        switch (state) {
            case CLOSED:
                // 连接初始状态，等待发送SYN包或接收SYN包
                if (syn && !ack) {
                    state = ConnectionState.SYN_SENT;
                }
                break;
            case SYN_SENT:
                // 已发送SYN包，等待SYN-ACK响应
                if (syn && ack) {
                    state = ConnectionState.SYN_RECEIVED;
                }
                break;
            case SYN_RECEIVED:
                // 收到SYN包并发送了SYN-ACK包，等待ACK确认连接建立
                if (!syn && ack) {
                    state = ConnectionState.ESTABLISHED;
                }
                break;
            case ESTABLISHED:
                // 连接已建立，可以进行数据通信
                // 如果发送或接收到FIN包，开始关闭连接的过程
                if (fin) {
                    state = ConnectionState.FIN_WAIT_1;
                }
                break;
            case FIN_WAIT_1:
                // 已发送FIN包，等待ACK响应
                if (ack) {
                    state = ConnectionState.FIN_WAIT_2;
                }
                break;
            case FIN_WAIT_2:
                // 收到对方的ACK响应FIN包，等待对方的FIN包
                if (fin) {
                    state = ConnectionState.TIME_WAIT;
                }
                break;
            case TIME_WAIT:
                // 收到对方的FIN包并发送ACK响应，等待一段时间以确保对方收到ACK
                if (ack) {
                    // 假设在TIME_WAIT状态后收到ACK，我们认为连接成功断开
                    state = ConnectionState.FINISHED;
                }
                break;
        }
    }


    public boolean isConnectionEstablished() {
        return state == ConnectionState.ESTABLISHED;
    }

    /**
     * 打印所有捕获到的连接的详细信息。
     * <p>
     * 该方法遍历所有存储的连接对象，并打印每个连接的源IP地址、目的IP地址、
     * 源端口号、目的端口号、连接状态、连接时长以及捕获到的数据包数量。
     * 这有助于快速诊断网络问题或分析网络流量。
     */
    public void printAllConnectionsInfo() {



        System.out.println("-------------------------------------------");
        System.out.println("源IP地址: " + this.getSourceIpAddress());
        System.out.println("目的IP地址: " + this.getDestinationIpAddress());
        System.out.println("源端口号: " + this.getSourcePort());
        System.out.println("目的端口号: " + this.getDestinationPort());
        System.out.println("连接状态: " + (this.getState()== ConnectionState.FINISHED ? "连接完成" : "未建立/进行中"));
        // 计算两个时间戳之间的差异
        Duration duration = Duration.between(this.getFirstPacketTimestamp(),this.getLastPacketTimestamp());
        System.out.println("连接时长: " +duration.getNano()+ "纳秒");
        System.out.println("数据包数量: " + this.getPackets().size());
        System.out.println("-------------------------------------------");
    }
}
