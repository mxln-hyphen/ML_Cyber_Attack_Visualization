package Innercommon.datastructure;

import Innercommon.Enum.HTTPMethod;
import Innercommon.Enum.NewConnectionState;
import lombok.Getter;
import lombok.Setter;
import org.pcap4j.packet.Packet;
import Innercommon.Enum.ConnectionState;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 记录一次TCP连接
 */
@Getter
@Setter
public class NewConnection {
    //@Transient
    //private List<Packet> packets = new ArrayList<>(); // 该连接中所有的数据包

    private Boolean isFromMySQL = false; //是否从数据库中获取

    @Id
    private String id;


    private String connectionId; //连接标识符


    private String sourceIpAddress; // 源IP地址(连接发起者地址)


    private String destinationIpAddress; // 目的IP地址


    private int sourcePort; // 源端口号


    private int destinationPort; // 目的端口号


    private NewConnectionState state = NewConnectionState.CLOSED; //连接状态


    private Boolean isOver; //连接是否正常关闭


    private Instant firstPacketTimestamp; // 第一个数据包的时间戳


    private Instant lastPacketTimestamp; // 最后一个数据包的时间戳


    private int connectionDuration; // 连接时长，单位：秒


    private int forwardPacketCount = 0; // 正向数据包数量


    private int backwardPacketCount = 0; // 反向数据包数量


    private Boolean isHTTP;  //是否是HTTP连接


    private HTTPMethod httpMethod;  //http请求方法


    private Boolean httpIsSuccess;  //http有没有成功返回一次200


    private Boolean isTemporary;  //是否为临时刷入

    private String ex2;

    private String ex3;

    private String ex4;


    public NewConnection() {
    }

    public NewConnection(String connectionId, String sourceIpAddress, String destinationIpAddress, int sourcePort, int destinationPort, NewConnectionState state, Boolean isOver, Instant firstPacketTimestamp, Instant lastPacketTimestamp, int connectionDuration, int forwardPacketCount, int backwardPacketCount, Boolean isHTTP, HTTPMethod httpMethod, Boolean httpIsSuccess,Boolean isTemporary) {
        this.connectionId = connectionId;
        this.sourceIpAddress = sourceIpAddress;
        this.destinationIpAddress = destinationIpAddress;
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        this.state = state;
        this.isOver = isOver;
        this.firstPacketTimestamp = firstPacketTimestamp;
        this.lastPacketTimestamp = lastPacketTimestamp;
        this.connectionDuration = connectionDuration;
        this.forwardPacketCount = forwardPacketCount;
        this.backwardPacketCount = backwardPacketCount;
        this.isHTTP = isHTTP;
        this.httpMethod = httpMethod;
        this.httpIsSuccess = httpIsSuccess;
        this.isTemporary = isTemporary;
    }
}
