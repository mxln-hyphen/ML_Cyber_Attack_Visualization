package com.itmxln.mldatasetprocessing.RealtimePacketCapture;

import Innercommon.datastructure.Connection;
import org.pcap4j.core.*;
import org.pcap4j.packet.IpPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import Innercommon.Enum.ConnectionState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实时根据设定时间进行一段时间的数据包捕获并分析
 */
public class PacketCapturer {
    private int time; // 捕获时长，单位为秒
    private final List<Packet> packets; // 存储捕获的数据包
    private int chosenIfs; //监听的网卡设备序号
    private PcapHandle handle;
    private Map<String, Connection> connections = new HashMap<>(); //记录所有的连接

    public PacketCapturer(int time,int ifsNum) {
        this.time = time;
        this.chosenIfs = ifsNum;
        this.packets = new ArrayList<>();
    }

    /**
     * 开始捕获，捕获时长为time秒
     *
     * @throws PcapNativeException
     * @throws InterruptedException
     * @throws NotOpenException
     */
    public void startCapture() throws PcapNativeException, InterruptedException, NotOpenException {
        List<PcapNetworkInterface> allDevs = Pcaps.findAllDevs();
        if (allDevs.isEmpty() || chosenIfs >= allDevs.size() || chosenIfs < 0) {
            System.out.println("选择的网卡不存在");
            return;
        }
        PcapNetworkInterface device = allDevs.get(chosenIfs);

        this.handle = device.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);

        // 使用独立线程进行数据包捕获
        Thread captureThread = new Thread(() -> {
            try {
                this.handle.loop(-1, (PacketListener) packet -> packets.add(packet));
            } catch (PcapNativeException e) {
                e.printStackTrace();
            } catch (NotOpenException e) {
                e.printStackTrace();
            }catch (InterruptedException e) {
                // 这里不需要做额外的操作，因为中断是由breakLoop()方法触发的预期行为
                System.out.println("数据包捕获已结束，捕获时长"+time+"秒");
            }
        });
        captureThread.start();

        // 指定时间后停止捕获
        Thread.sleep(time * 1000L);
        this.handle.breakLoop();

        // 等待捕获线程结束
        captureThread.join();

        this.handle.close();
    }

    /**
     * 返回捕获到数据包的数量
     * @return
     */
    public int getTotalPacketsCaptured() {
        return packets.size();
    }

    /**
     * 将数据包根据TCP连接进行分类
     */
    public void classifyPackets() {
        for (Packet packet : packets) {
            if (packet.contains(IpPacket.class) && packet.contains(TcpPacket.class)) {
                IpPacket ipPacket = packet.get(IpPacket.class);
                TcpPacket tcpPacket = packet.get(TcpPacket.class);

                String sourceIp = ipPacket.getHeader().getSrcAddr().toString();
                String destIp = ipPacket.getHeader().getDstAddr().toString();
                int sourcePort = tcpPacket.getHeader().getSrcPort().valueAsInt();
                int destPort = tcpPacket.getHeader().getDstPort().valueAsInt();

                // 生成连接ID
                String connectionId = generateConnectionId(sourceIp, destIp, sourcePort, destPort);

                // 使用connectionId来检索或创建对应的Connection实例
                Connection connection = connections.get(connectionId);
                if (connection == null) {
                    //connection = new Connection(sourceIp, destIp, sourcePort, destPort);
                    connections.put(connectionId, connection);
                }
                //需要在新增数据包时获取当前时间戳
                //connection.addPacket(packet);
            }
        }
    }

    /**
     * 生成基于IP地址和端口号的唯一连接ID。
     * <p>
     * 此方法确保无论数据包的方向如何，相同连接的数据包都能生成相同的ID。
     * 它通过比较源和目的IP地址，确保较小的IP地址总是排在前面，从而生成一个一致的、
     * 方向无关的连接ID。
     *
     * @param srcIp 源IP地址
     * @param destIp 目的IP地址
     * @param srcPort 源端口号
     * @param destPort 目的端口号
     * @return 生成的唯一连接ID，格式为"小IP地址:小端口号<->大IP地址:大端口号"
     */
    private String generateConnectionId(String srcIp, String destIp, int srcPort, int destPort) {
        // 确保IP地址的顺序，使得较小的IP地址总是排在前面
        boolean ipOrder = srcIp.compareTo(destIp) < 0;

        // 根据IP地址顺序，组合IP地址和端口号
        String orderedSrcIp = ipOrder ? srcIp : destIp;
        String orderedDestIp = ipOrder ? destIp : srcIp;
        int orderedSrcPort = ipOrder ? srcPort : destPort;
        int orderedDestPort = ipOrder ? destPort : srcPort;

        // 生成并返回连接ID
        return orderedSrcIp + ":" + orderedSrcPort + "<->" + orderedDestIp + ":" + orderedDestPort;
    }

    // 获取所有连接
    public Map<String, Connection> getConnections() {
        return connections;
    }

    /**
     * 打印所有捕获到的连接的详细信息。
     * <p>
     * 该方法遍历所有存储的连接对象，并打印每个连接的源IP地址、目的IP地址、
     * 源端口号、目的端口号、连接状态、连接时长以及捕获到的数据包数量。
     * 这有助于快速诊断网络问题或分析网络流量。
     */
    public void printAllConnectionsInfo() {
        if (connections.isEmpty()) {
            System.out.println("没有捕获到任何连接。");
            return;
        }

        System.out.println("捕获到的连接信息：");
        for (Connection connection : connections.values()) {
            System.out.println("-------------------------------------------");
            System.out.println("源IP地址: " + connection.getSourceIpAddress());
            System.out.println("目的IP地址: " + connection.getDestinationIpAddress());
            System.out.println("源端口号: " + connection.getSourcePort());
            System.out.println("目的端口号: " + connection.getDestinationPort());
            System.out.println("连接状态: " + (connection.getState()== ConnectionState.FINISHED ? "连接完成" : "未建立/进行中"));
            System.out.println("连接时长: " + connection.getConnectionDuration() + "秒");
            System.out.println("数据包数量: " + connection.getPackets().size());

        }
        System.out.println("-------------------------------------------");
    }




    public static void main(String[] args) throws IOException, NotOpenException, PcapNativeException, InterruptedException {
        PacketCapturer capturer = new PacketCapturer(10,10); // 设置捕获时长为10秒
        capturer.startCapture();
        System.out.println("总共捕获到的数据包数量: " + capturer.getTotalPacketsCaptured());
        capturer.classifyPackets();
        capturer.printAllConnectionsInfo();
    }
}
