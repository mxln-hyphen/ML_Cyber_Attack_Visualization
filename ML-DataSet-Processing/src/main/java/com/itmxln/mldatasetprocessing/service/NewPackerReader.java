package com.itmxln.mldatasetprocessing.service;

import Innercommon.Enum.ConnectionState;
import Innercommon.datastructure.Connection;
import Innercommon.datastructure.NewConnection;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Select;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.EOFException;
import java.time.Instant;
import java.util.HashMap;

@Service
@Slf4j
public class NewPackerReader {

    //储存所有未完成的连接
    private HashMap<String, NewConnection> map = new HashMap<>();

    //最大储存连接数
    private int MAXCONNECTIONNUM = 1000;

    //表名
    private String fileName;

    @Autowired
    private NewConnectionService newConnectionService;

    /**
     * 从pcap文件中读取所有数据包，并根据它们属于哪个连接进行分类。
     *
     * @param filePath pcap文件的路径
     */
    public void readPcapFile(String filePath) {
        fileName = "data_1";
        try {
            PcapHandle handle = Pcaps.openOffline(filePath);
            int packetCount = 0;
            while (true) {
                try {
                    //读取数据包
                    Packet packet = handle.getNextPacketEx();
                    // 获取数据包的时间戳
                    Instant timestamp = handle.getTimestamp().toInstant();
                    // 根据数据包ip生成唯一标识符
                    String connectionId = generateConnectionId(packet);

                    //连接
                    NewConnection connection;


                    //获取connection
                    //查询connection是否已经建立
                    boolean isInMap = map.containsKey(connectionId);
                    if (!isInMap) {
                        //数据库能否查到
                        connection = newConnectionService.selectTemporaryConnectionByConnectionId(fileName, connectionId);
                        if (connection == null) {
                            //map中connection是否太多了
                            boolean tooMuchConnection = map.size() > MAXCONNECTIONNUM;
                            if (tooMuchConnection) {
                                //把所有connection刷数据库，并标记为临时刷入
                                insertAllConnection(map);
                            }
                            //建立新connection并写入Map
                            connection = generateNewConnection(packet, timestamp);
                            map.put(connectionId, connection);
                        }
                    } else {
                        //从map中获取connection
                        connection = map.get(connectionId);
                    }

                    //更新connection信息，记得把是否是临时数据改为false



                    //连接是否结束
                    boolean isConnectionOver = false;

                    if (isConnectionOver) {
                        //写数据库
                    }
                } catch (EOFException e) {
                    break; // 文件结束
                }
            }
            // 把所有connection刷数据库，并标记为临时刷入

            handle.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //生成数据包的唯一标识符，每个标识符对应的连接同一时间在内存中只会存在一个
    private String generateConnectionId(Packet packet) {
        //获取TCP，IP部分
        String srcIp = newConnectionService.getSourceIp(packet);

        String dstIp = newConnectionService.getDstIp(packet);

        int srcPort = newConnectionService.getSourcePort(packet);

        int dstPort = newConnectionService.getDstPort(packet);

        // 生成并返回连接ID
        // ID一定是小ip在前，大ip在后
        String connectionId = srcIp.compareTo(dstIp) <= 0 ?
                srcIp + ":" + srcPort + "<->" + dstIp + ":" + dstPort :
                dstIp + ":" + dstPort + "<->" + srcIp + ":" + srcPort;

        return connectionId;
    }


    /**
     * 把map中所有连接刷进数据库，并且标记为临时数据
     */
    private void insertAllConnection(HashMap<String, NewConnection> map) {
        int count = 0;

        //遍历map中所有连接
        for (NewConnection connection : map.values()) {
            //把连接设置为临时
            connection.setIsTemporary(true);
            //将连接写入数据库
            newConnectionService.writeMySQL(fileName, connection);
            //计数
            count++;
        }

        log.info("一共写入" + count + "个临时数据");
    }

    /**
     * 根据数据包生成新连接
     *
     * @return
     */
    private NewConnection generateNewConnection(Packet packet, Instant timestamp) {
        //生成新连接
        NewConnection connection = new NewConnection();


        //设置必要字段
        //设置唯一标识符
        connection.setConnectionId(generateConnectionId(packet));

        //设置ip和端口信息，一次连接的方向在创造连接时确定
        connection.setSourceIpAddress(newConnectionService.getSourceIp(packet));
        connection.setDestinationIpAddress(newConnectionService.getDstIp(packet));
        connection.setSourcePort(newConnectionService.getSourcePort(packet));
        connection.setDestinationPort(newConnectionService.getDstPort(packet));

        //设置连接第一个数据包的时间
        // 获取数据包的时间戳
        connection.setFirstPacketTimestamp(timestamp);

        log.info("连接生成成功");

        return connection;

    }

}
