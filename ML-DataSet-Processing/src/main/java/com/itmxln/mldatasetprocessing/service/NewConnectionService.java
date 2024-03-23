package com.itmxln.mldatasetprocessing.service;

import Innercommon.Enum.ConnectionState;
import Innercommon.Enum.HTTPMethod;
import Innercommon.Enum.NewConnectionState;
import Innercommon.datastructure.NewConnection;
import Innercommon.dojo.MySQLConnection;
import com.itmxln.mldatasetprocessing.mysql.mapper.DataMapper;
import com.itmxln.mldatasetprocessing.utils.DateUtils;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.pcap4j.packet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class NewConnectionService {

    @Autowired
    private DataMapper dataMapper;


    /**
     * 将一个连接写入数据库,在写入前删除临时数据
     *
     * @param tableName
     * @param newConnection
     * @return
     */
    public int writeMySQL(String tableName, NewConnection newConnection) {
        //判空
        if (newConnection == null) {
            log.error("错误：向数据库写入空数据！");
            return 0;
        }

        //如果数据库中已经有该数据
        if (newConnection.getIsFromMySQL()) {
            //根据id删除数据库信息
            deleteTemporaryConnectionByConnectionId(tableName,newConnection.getConnectionId());
        }


        //写数据库
        Integer count = dataMapper.insert(tableName,
                generateMySQLConnection(newConnection)
        );

        if (count == 1) {
            log.info("写入成功");
        }

        return 1;
    }


    /**
     * 根据ConnectionId在MySQL中查询是否存在临时数据
     */
    NewConnection selectTemporaryConnectionByConnectionId(String tableName, String connectionId) {
        //根据id查询
        List<MySQLConnection> connection = dataMapper.selectTemporaryConnectionByConnectionId(tableName, connectionId);

        if (connection.size() > 1) {
            log.error("找到多余一个临时记录！");
        }

        if (connection.size() == 1) {
            log.info("找到一条临时记录");
        }
        NewConnection ret = generateMySQLConnection(connection.get(0));
        ret.setIsFromMySQL(true);
        return generateMySQLConnection(connection.get(0));
    }

    /**
     * 根据ConnectionId在MySQL中删除临时数据
     */
    public void deleteTemporaryConnectionByConnectionId(String tableName, String connectionId) {
        //根据id删除
        dataMapper.deleteTemporaryConnectionByConnectionId(tableName, connectionId);
    }



    /**
     * 把NewConnection类转换为可以写入数据库的MySQLConnection类
     */
    MySQLConnection generateMySQLConnection(NewConnection newConnection) {
        MySQLConnection mySQLConnection = new MySQLConnection();
        mySQLConnection.setConnectionId(newConnection.getConnectionId());
        mySQLConnection.setSourceIpAddress(newConnection.getSourceIpAddress());
        mySQLConnection.setDestinationIpAddress(newConnection.getDestinationIpAddress());
        mySQLConnection.setSourcePort(newConnection.getSourcePort());
        mySQLConnection.setDestinationPort(newConnection.getDestinationPort());
        mySQLConnection.setState(newConnection.getState().ordinal());
        mySQLConnection.setIsOver(newConnection.getIsOver());
        mySQLConnection.setFirstPacketTimestamp(DateUtils.timestampToDate(newConnection.getFirstPacketTimestamp()));
        mySQLConnection.setLastPacketTimestamp(DateUtils.timestampToDate(newConnection.getLastPacketTimestamp()));
        mySQLConnection.setConnectionDuration(newConnection.getConnectionDuration());
        mySQLConnection.setForwardPacketCount(newConnection.getForwardPacketCount());
        mySQLConnection.setBackwardPacketCount(newConnection.getBackwardPacketCount());
        mySQLConnection.setIsHTTP(newConnection.getIsHTTP());
        mySQLConnection.setHttpMethod(newConnection.getHttpMethod().ordinal());
        mySQLConnection.setHttpIsSuccess(newConnection.getHttpIsSuccess());
        mySQLConnection.setIsTemporary(newConnection.getIsTemporary());
        mySQLConnection.setEx2(newConnection.getEx2());
        mySQLConnection.setEx3(newConnection.getEx3());
        mySQLConnection.setEx4(newConnection.getEx4());

        return mySQLConnection;
    }

    /**
     * 把从数据库查询到的MySQLConnection类转化为NewConnection
     */
    NewConnection generateMySQLConnection(MySQLConnection mySQLConnection) {
        NewConnection newConnection = new NewConnection();
        newConnection.setConnectionId(mySQLConnection.getConnectionId());
        newConnection.setSourceIpAddress(mySQLConnection.getSourceIpAddress());
        newConnection.setDestinationIpAddress(mySQLConnection.getDestinationIpAddress());
        newConnection.setSourcePort(mySQLConnection.getSourcePort());
        newConnection.setDestinationPort(mySQLConnection.getDestinationPort());
        newConnection.setState(NewConnectionState.codeOf(mySQLConnection.getState()));
        newConnection.setIsOver(mySQLConnection.getIsOver());
        newConnection.setFirstPacketTimestamp(Instant.ofEpochMilli(mySQLConnection.getFirstPacketTimestamp().getTime()));
        newConnection.setLastPacketTimestamp(Instant.ofEpochMilli(mySQLConnection.getLastPacketTimestamp().getTime()));
        newConnection.setConnectionDuration(newConnection.getConnectionDuration());
        newConnection.setForwardPacketCount(newConnection.getForwardPacketCount());
        newConnection.setBackwardPacketCount(newConnection.getBackwardPacketCount());
        newConnection.setIsHTTP(newConnection.getIsHTTP());
        newConnection.setHttpMethod(HTTPMethod.codeOf(mySQLConnection.getHttpMethod()));
        newConnection.setHttpIsSuccess(mySQLConnection.getHttpIsSuccess());
        newConnection.setIsTemporary(mySQLConnection.getIsTemporary());
        newConnection.setEx2(mySQLConnection.getEx2());
        newConnection.setEx3(mySQLConnection.getEx3());
        newConnection.setEx4(mySQLConnection.getEx4());

        return newConnection;
    }

    /**
     * 获取源ip
     * @return
     */
    public String getSourceIp(Packet packet){
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
     * @return
     */
    public String getDstIp(Packet packet){
        IpPacket ipPacket = packet.get(IpPacket.class);
        if (ipPacket instanceof IpV4Packet) {
            return  ((IpV4Packet) ipPacket).getHeader().getDstAddr().toString().substring(1);
        } else if (ipPacket instanceof IpV6Packet) {
            return  ((IpV6Packet) ipPacket).getHeader().getDstAddr().toString().substring(1);
        }
        return "";
    }

    /**
     * 获取源端口
     * @return
     */
    public int getSourcePort(Packet packet){
        TcpPacket tcpPacket = packet.get(TcpPacket.class);

        return tcpPacket.getHeader().getSrcPort().valueAsInt();
    }

    /**
     * 获取目的端口
     * @return
     */
    public int getDstPort(Packet packet){
        TcpPacket tcpPacket = packet.get(TcpPacket.class);

        return tcpPacket.getHeader().getDstPort().valueAsInt();
    }


}
