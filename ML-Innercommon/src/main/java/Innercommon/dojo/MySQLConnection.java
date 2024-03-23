package Innercommon.dojo;

import Innercommon.Enum.HTTPMethod;
import Innercommon.Enum.NewConnectionState;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.Date;

@Getter
@Setter
public class MySQLConnection {

    @Id
    private String id;


    private String connectionId; //连接标识符


    private String sourceIpAddress; // 源IP地址(连接发起者地址)


    private String destinationIpAddress; // 目的IP地址


    private int sourcePort; // 源端口号


    private int destinationPort; // 目的端口号


    private int state; //连接状态


    private Boolean isOver; //连接是否正常关闭


    private Date firstPacketTimestamp; // 第一个数据包的时间戳


    private Date lastPacketTimestamp; // 最后一个数据包的时间戳


    private int connectionDuration; // 连接时长，单位：秒


    private int forwardPacketCount; // 正向数据包数量


    private int backwardPacketCount; // 反向数据包数量


    private Boolean isHTTP;  //是否是HTTP连接


    private int httpMethod;  //http请求方法


    private Boolean httpIsSuccess;  //http有没有成功返回一次200


    private Boolean isTemporary;  //是否为临时刷入

    private String ex2;

    private String ex3;

    private String ex4;
}
