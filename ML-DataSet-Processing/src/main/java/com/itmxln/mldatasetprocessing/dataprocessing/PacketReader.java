package com.itmxln.mldatasetprocessing.dataprocessing;

import Innercommon.Enum.ConnectionState;
import Innercommon.datastructure.Connection;
import com.itmxln.mldatasetprocessing.elastic.ConnectionRepository;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.EOFException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class PacketReader {
    @Autowired
    private ConnectionRepository connectionRepository;

    private List<Connection> successConnections = new ArrayList<>();    //储存正常结束的记录

    private Map<String, Connection> connections = new HashMap<>();      //储存还未结束的记录

    /**
     * 从pcap文件中读取所有数据包，并根据它们属于哪个连接进行分类。
     *
     * @param filePath pcap文件的路径
     */
    public void readPcapFile(String filePath) {
        try {
            PcapHandle handle = Pcaps.openOffline(filePath);
            int packetCount = 0;
            while (true){
                try {
                    //读取数据包
                    Packet packet = handle.getNextPacketEx();
                    // 获取数据包的时间戳
                    Instant timestamp = handle.getTimestamp().toInstant();
                    //为数据包找到所属连接并更新信息
                    Connection connection = getConnectionForPacket(packet,timestamp, connections);

                    if(connection==null){
                        continue;
                    }

                    // 检查连接是否已结束
                    if (connection.getState() == ConnectionState.FINISHED) {
                        //将正常结束的记录储存到数组中
                        successConnections.add(connection);
                        //将正常结束的记录从映射中移除
                        connections.remove(connection.getConnectionId());

                        //当正常结束的记录大于5000条时，储存到elastic中
                        if (successConnections.size() >= 5000) {
                            saveSuccessConnections();
                        }
                    }

                    packetCount++; // 更新计数器
                    System.out.print("\rNumber of packets read: " + packetCount); // 打印计数器的当前值

                    // 每500000个数据包，存储并清空connections
                    if (packetCount % 500000 == 0) {
                        saveAndClearConnections();
                    }

                } catch (EOFException e) {
                    break; // 文件结束
                }
            }
            // 确保最后一批数据也被保存
            saveSuccessConnections();
            saveAndClearConnections();
            handle.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将connections中的所有连接储存到elasticsearch中
     */
    private void saveAndClearConnections() {
        connectionRepository.saveAll(connections.values());
        System.out.println("Batch saved. Number of connections: " + connections.size()
                +", Number of success connections:"+successConnections.size());
        connections.clear(); // 清空connections以释放内存
    }

    /**
     * 将正常结束的记录储存到elasticsearch中
     */
    private void saveSuccessConnections() {
        connectionRepository.saveAll(successConnections);
        System.out.println("Batch of successConnections saved. Number of connections: " + successConnections.size());
        successConnections.clear(); // 清空successConnections以释放内存
    }

    /**
     * 查看数据包所属的连接是否存在，如果存在，更新连接信息，否则新增一个连接
     *
     * @param packet 数据包
     * @return 生成的连接ID，如果数据包不是TCP数据包，则可能返回null
     */
    public static Connection getConnectionForPacket(Packet packet, Instant timestamp, Map<String, Connection> connections) {
        if (!packet.contains(TcpPacket.class)) {
            return null; // 非TCP包，不处理
        }

        TcpPacket tcpPacket = packet.get(TcpPacket.class);
        IpPacket ipPacket = packet.get(IpPacket.class);
        String srcIp = "", dstIp = "";
        if (ipPacket instanceof IpV4Packet) {
            srcIp = ((IpV4Packet) ipPacket).getHeader().getSrcAddr().toString().substring(1);
            dstIp = ((IpV4Packet) ipPacket).getHeader().getDstAddr().toString().substring(1);
        } else if (ipPacket instanceof IpV6Packet) {
            srcIp = ((IpV6Packet) ipPacket).getHeader().getSrcAddr().toString().substring(1);
            dstIp = ((IpV6Packet) ipPacket).getHeader().getDstAddr().toString().substring(1);
        }

        int srcPort = tcpPacket.getHeader().getSrcPort().valueAsInt();
        int dstPort = tcpPacket.getHeader().getDstPort().valueAsInt();

        // 生成并返回连接ID
        String connectionId = srcIp.compareTo(dstIp) <= 0 ?
                srcIp + ":" + srcPort + "<->" + dstIp + ":" + dstPort :
                dstIp + ":" + dstPort + "<->" + srcIp + ":" + srcPort;


        //查看连接是否存在
        Connection connection = connections.get(connectionId);
        if (connection == null) {
            connection = new Connection(connectionId,srcIp, dstIp, srcPort, dstPort);
            connections.put(connectionId, connection);
        }

        //对连接新增数据包
        connection.addPacket(packet,timestamp);

        return connection;
    }


    public static void main(String[] args) {
        // // 替换为你的pcap文件路径
        // String pcapFilePath = "D:\\bishe\\pcap\\Monday-WorkingHours.pcap";
        //
        // // 准备存储和管理连接对象的映射
        // Map<String, Connection> connections = new HashMap<>();
        //
        // // 调用readPcapFile方法来读取pcap文件并填充connections映射
        // PacketReader.readPcapFile(pcapFilePath, connections);
        //
        // // 遍历并打印每个连接的信息及其中包含的数据包数量
        // System.out.println("Connections and their packet counts:");
        // for (Map.Entry<String, Connection> entry : connections.entrySet()) {
        //     String connectionId = entry.getKey();
        //     Connection connection = entry.getValue();
        //     System.out.println(connectionId + " has " + connection.getPackets().size() + " packets.");
        //
        //     // 可以进一步打印连接的其他信息，如状态、时长等
        //     // 例如:
        //     connection.printAllConnectionsInfo();
        // }
    }
}
