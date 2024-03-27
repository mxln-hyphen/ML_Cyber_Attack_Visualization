package com.itmxln.mldatasetprocessing.service;

import Innercommon.Enum.NewConnectionState;
import Innercommon.datastructure.NewConnection;
import com.itmxln.mldatasetprocessing.mysql.mapper.DataMapper;
import lombok.extern.slf4j.Slf4j;
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
    private int MAXCONNECTIONNUM = 3000;

    //表名
    private String tableName;


    @Autowired
    private NewConnectionService newConnectionService;

    @Autowired
    private PacketService packetService;

    @Autowired
    private DataMapper dataMapper;

    /**
     * 从pcap文件中读取所有数据包，并根据它们属于哪个连接进行分类。
     *
     * @param filePath pcap文件的路径
     */
    public void readPcapFile(String filePath) {
        //获取表名并生成表
        tableName = createTable(filePath);
        int packetCount = 0;
        try {
            PcapHandle handle = Pcaps.openOffline(filePath);
            while (true) {
                try {
                    //读取数据包
                    Packet packet = handle.getNextPacketEx();
                    // 获取数据包的时间戳
                    Instant timestamp = handle.getTimestamp().toInstant();

                    if (!packet.contains(TcpPacket.class)) {
                        continue; // 非TCP包，不处理
                    }

                    // 根据数据包ip生成唯一标识符
                    String connectionId = generateConnectionId(packet);


                    //连接
                    NewConnection connection=null;

                    //获取connection
                    //查询connection是否已经建立
                    boolean isInMap = map.containsKey(connectionId);
                    if (!isInMap) {
                        //尝试在数据库中查询该次连接
                        //connection = newConnectionService.selectTemporaryConnectionByConnectionId(tableName, connectionId);
                    } else {
                        //从map中获取connection
                        connection = map.get(connectionId);
                    }

                    //检查该数据包是否是一个开启连接的数据包
                    if (packetService.isStart(packet)) {//如果是
                        if (connection != null) {//有一个已经存在的连接
                            //把这个连接提前终止
                            connection.setIsOver(true);
                            //状态设置为超时
                            connection.setState(NewConnectionState.TIME_OVER);
                            //在map中移除这个连接
                            map.remove(connectionId);
                            newConnectionService.writeMySQL(tableName, connection);
                            connection = null;
                        }
                        //检查一下连接数是否太多
                        checkMapSize(map);
                        //新建一个连接
                        connection = generateNewConnection(packet, timestamp);
                        //放进Map
                        map.put(connectionId, connection);
                    }

                    if (connection != null) {
                        //更新connection信息
                        updateNewConnection(connection, packet, timestamp);

                        //连接是否结束
                        boolean isConnectionOver = connection.getIsOver();

                        if (isConnectionOver) {
                            //写数据库
                            newConnectionService.writeMySQL(tableName, connection);
                            //在map中移除这个连接
                            map.remove(connectionId);
                        }
                    }
                } catch (EOFException e) {
                    break; // 文件结束
                } finally {
                    packetCount++; // 更新计数器
                    System.out.print("\rNumber of packets read: " + packetCount); // 打印计数器的当前值
                    if (packetCount % 1000000 == 0) {
                        log.info("map大小" + map.size() + "\n");
                    }
                }
            }
            // 刷一次盘
            insertAllConnection(map);

            handle.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成数据包的唯一标识符，每个标识符对应的连接同一时间在内存中只会存在一个
     */
    private String generateConnectionId(Packet packet) {
        //获取TCP，IP部分
        String srcIp = packetService.getSourceIp(packet);

        String dstIp = packetService.getDstIp(packet);

        int srcPort = packetService.getSourcePort(packet);

        int dstPort = packetService.getDstPort(packet);

        // 生成并返回连接ID
        // ID一定是小ip在前，大ip在后
        String connectionId = srcIp.compareTo(dstIp) <= 0 ?
                srcIp + ":" + srcPort + "<->" + dstIp + ":" + dstPort :
                dstIp + ":" + dstPort + "<->" + srcIp + ":" + srcPort;

        return connectionId;
    }

    /**
     * 检查内存中的连接是否太多了，如果太多就刷一次盘
     */
    private void checkMapSize(HashMap<String, NewConnection> map) {
        if (map.size() > MAXCONNECTIONNUM) {//如果超过了阈值
            //把map中所有数据刷进数据库
            insertAllConnection(map);
        }
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
            newConnectionService.writeMySQL(tableName, connection);
            //计数
            count++;
        }

        log.info("一共写入" + count + "个临时数据,目前Map大小" + map.size() + "\n");
        //清空map
        map.clear();
        map = null;

        //创建一个空的map
        map = new HashMap<>();

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
        connection.setSourceIpAddress(packetService.getSourceIp(packet));
        connection.setDestinationIpAddress(packetService.getDstIp(packet));
        connection.setSourcePort(packetService.getSourcePort(packet));
        connection.setDestinationPort(packetService.getDstPort(packet));

        //设置连接第一个数据包的时间
        // 获取数据包的时间戳
        connection.setFirstPacketTimestamp(timestamp);

        //设置状态
        connection.setState(NewConnectionState.CLOSED);

        //log.info("连接生成成功");

        return connection;
    }

    /**
     * 根据数据包更新连接
     */
    private void updateNewConnection(NewConnection connection, Packet packet, Instant timestamp) {
        //更新状态
        connection.setState(packetService.getState(packet, connection.getState()));

        //确认连接现在是否关闭了
        if (connection.getState() == NewConnectionState.FINISHED
                || connection.getState() == NewConnectionState.RST_FINISHED) {
            connection.setIsOver(true);
        }

        //更新最后一个数据包时间戳
        connection.setLastPacketTimestamp(timestamp);

        //更新连接时长
        connection.updateDuration();

        //更新数据包数量
        if (checkForward(packet, connection)) {//正向
            connection.setForwardPacketCount(connection.getForwardPacketCount() + 1);
        } else {//逆向
            connection.setBackwardPacketCount(connection.getBackwardPacketCount() + 1);
        }

        //Http协议pcap4j不支持解析，后续看看要不要手写一下http的协议读取

    }

    /**
     * 判断数据包在该连接中属于正向还是逆向
     */
    private boolean checkForward(Packet packet, NewConnection connection) {
        //获取源ip
        String sourceIp = packetService.getSourceIp(packet);

        //判断方向
        if (connection.getSourceIpAddress().equals(sourceIp)) {
            return true;
        }

        return false;
    }

    /**
     * 根据文件名创建对应的表
     */
    private String createTable(String fileName){
        //生成表名
        String substring = fileName.substring(fileName.lastIndexOf("\\") + 1, fileName.lastIndexOf("."));
        String[] split = substring.split("-");
        String tableName = split[0] + "_" + split[1];

        //如果已经存在，删除表
        dataMapper.dropExistTable(tableName);

        //创建表
        dataMapper.createTable(tableName);
        return tableName;
    }


}
