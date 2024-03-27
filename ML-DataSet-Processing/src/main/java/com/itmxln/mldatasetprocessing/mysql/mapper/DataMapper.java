package com.itmxln.mldatasetprocessing.mysql.mapper;


import Innercommon.datastructure.NewConnection;
import Innercommon.dojo.MySQLConnection;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface DataMapper extends BaseMapper<NewConnection> {


    /**
     * 插入一条数据
     * @param tableName
     * @param mySQLConnection
     * @return
     */
    @Insert("INSERT INTO ${tableName} (\n" +
            "	`connectionId`,\n" +
            "	`sourceIpAddress`,\n" +
            "	`destinationIpAddress`,\n" +
            "	`sourcePort`,\n" +
            "	`destinationPort`,\n" +
            "	`state`,\n" +
            "	`isOver`,\n" +
            "	`firstPacketTimestamp`,\n" +
            "	`lastPacketTimestamp`,\n" +
            "	`connectionDuration`,\n" +
            "	`forwardPacketCount`,\n" +
            "	`backwardPacketCount`,\n" +
            "	`isHTTP`,\n" +
            "	`httpMethod`,\n" +
            "	`httpIsSuccess`,\n" +
            "	`isTemporary`,\n" +
            "	`ex2`,\n" +
            "	`ex3`,\n" +
            "	`ex4` \n" +
            ")\n" +
            "VALUES\n" +
            "	(\n" +
            "		#{mySQLConnection.connectionId},\n" +
            "		#{mySQLConnection.sourceIpAddress},\n" +
            "		#{mySQLConnection.destinationIpAddress},\n" +
            "		#{mySQLConnection.sourcePort},\n" +
            "		#{mySQLConnection.destinationPort},\n" +
            "		#{mySQLConnection.state},\n" +
            "		#{mySQLConnection.isOver},\n" +
            "		#{mySQLConnection.firstPacketTimestamp},\n" +
            "		#{mySQLConnection.lastPacketTimestamp},\n" +
            "		#{mySQLConnection.connectionDuration},\n" +
            "		#{mySQLConnection.forwardPacketCount},\n" +
            "		#{mySQLConnection.backwardPacketCount},\n" +
            "		#{mySQLConnection.isHTTP},\n" +
            "		#{mySQLConnection.httpMethod},\n" +
            "		#{mySQLConnection.httpIsSuccess},\n" +
            "		#{mySQLConnection.isTemporary},\n" +
            "		#{mySQLConnection.ex2},\n" +
            "		#{mySQLConnection.ex3},\n" +
            "		#{mySQLConnection.ex4} \n" +
            "	);")
    Integer insert(@Param("tableName")String tableName,
                  @Param("mySQLConnection") MySQLConnection mySQLConnection
    );

    /**
     * 查询对应connectionid是否存在临时数据
     * @param tableName
     * @param connectionId
     * @return
     */
    @Select("SELECT * FROM ${tableName} where  isTemporary = 1  and connectionId = #{connectionId} ;")
    List<MySQLConnection> selectTemporaryConnectionByConnectionId(@Param("tableName")String tableName,
                                                               @Param("connectionId") String connectionId);

    /**
     * 删除临时数据
     * @param tableName
     * @param connectionId
     * @return
     */
    @Delete("DELETE from ${tableName} where isTemporary = 1  and connectionId = #{connectionId};")
    Integer deleteTemporaryConnectionByConnectionId(@Param("tableName")String tableName,
                                                    @Param("connectionId") String connectionId);

    /**
     * 根据表名新建 连接记录数据库
     * @param tableName
     */
    @Update("CREATE TABLE ${tableName} (\n" +
            "  `id` int NOT NULL AUTO_INCREMENT,\n" +
            "  `connectionId` varchar(255) DEFAULT NULL COMMENT '连接标识符',\n" +
            "  `sourceIpAddress` varchar(255) DEFAULT NULL COMMENT '源IP地址(连接发起者地址)',\n" +
            "  `destinationIpAddress` varchar(255) DEFAULT NULL COMMENT '目的IP地址',\n" +
            "  `sourcePort` int DEFAULT NULL COMMENT '源端口号',\n" +
            "  `destinationPort` int DEFAULT NULL COMMENT '目的端口号',\n" +
            "  `state` int DEFAULT NULL COMMENT '连接状态\\r\\n0:   CLOSED, // 连接已关闭或四次挥手完成后的最终状态\\r\\n1:    SYN_SENT, // 已发送SYN包，等待SYN-ACK响应\\r\\n2:    SYN_RECEIVED, // 收到SYN包并发送了SYN-ACK包，等待ACK\\r\\n3:    ESTABLISHED, // 连接已建立，三次握手完成\\r\\n4:    FIN_WAIT_1, // 主动关闭连接，已发送FIN包\\r\\n5:    FIN_WAIT_2, // 收到对方的ACK响应FIN包，等待对方的FIN包\\r\\n6:    FINISHED // 连接完成并且成功断开\\r\\n7:    RST_FINISHED // 收到RST包，发生异常，直接中断连接\\r\\n8:    TIME_OVER //超时，没有接收到后续的包',\n" +
            "  `isOver` tinyint DEFAULT NULL COMMENT '连接是否正常关闭',\n" +
            "  `firstPacketTimestamp` datetime DEFAULT NULL COMMENT '第一个数据包的时间戳',\n" +
            "  `lastPacketTimestamp` datetime DEFAULT NULL COMMENT '最后一个数据包的时间戳',\n" +
            "  `connectionDuration` int DEFAULT NULL COMMENT '连接时长',\n" +
            "  `forwardPacketCount` int DEFAULT NULL COMMENT '正向数据包数量',\n" +
            "  `backwardPacketCount` int DEFAULT NULL COMMENT '反向数据包数量',\n" +
            "  `isHTTP` tinyint DEFAULT NULL COMMENT '是否是HTTP连接',\n" +
            "  `httpMethod` int DEFAULT NULL COMMENT 'http请求方法',\n" +
            "  `httpIsSuccess` tinyint DEFAULT NULL COMMENT 'http有没有成功返回一次200',\n" +
            "  `isTemporary` tinyint DEFAULT NULL COMMENT '是否为临时刷入',\n" +
            "  `ex2` varchar(255) DEFAULT NULL,\n" +
            "  `ex3` varchar(255) DEFAULT NULL,\n" +
            "  `ex4` varchar(255) DEFAULT NULL,\n" +
            "  PRIMARY KEY (`id`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;")
    void createTable(@Param("tableName") String tableName);

    /**
     * 根据表名删除表
     */
    @Update({"drop table if exists ${tableName}"})
    void dropExistTable(@Param("tableName") String tableName);



}
