package com.itmxln.mldatasetprocessing.mysql.mapper;


import Innercommon.datastructure.NewConnection;
import Innercommon.dojo.MySQLConnection;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface DataMapper extends BaseMapper<NewConnection> {





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


    @Select("SELECT * FROM ${tableName} where  isTemporary = 1  and connectionId = #{connectionId} ;")
    List<MySQLConnection> selectTemporaryConnectionByConnectionId(@Param("tableName")String tableName,
                                                               @Param("connectionId") String connectionId);


    @Delete("DELETE from ${tableName} where isTemporary = 1  and connectionId = #{connectionId};")
    Integer deleteTemporaryConnectionByConnectionId(@Param("tableName")String tableName,
                                                    @Param("connectionId") String connectionId);


}
