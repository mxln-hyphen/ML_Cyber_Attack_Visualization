package com.itmxln.mldatasetprocessing.mysql.mapper;

import Innercommon.datastructure.Entropy;
import Innercommon.dojo.MySQLConnection;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface EntropyMapper {

    /**
     * 根据时间查询连接数据
     *
     * @param tableName
     * @return
     */
    @Select("SELECT * FROM ${tableName} force index (time) WHERE firstPacketTimestamp <= '${time}' AND lastPacketTimestamp >= '${time}';")
    List<MySQLConnection> selectConnectionBySecond(@Param("tableName") String tableName,
                                                   @Param("time") String time);

    /**
     * 往数据库插入熵值数据
     */
    @Insert("INSERT INTO ${tableName} (`time`, `connectionNum`, `sourceIpValue`, `destinationIpValue`, `sourcePortValue`, `destinationPortValue`) " +
            "VALUES (#{entropy.time}, #{entropy.connectionNum}, #{entropy.sourceIpValue}, #{entropy.destinationIpValue}," +
            " #{entropy.sourcePortValue}, #{entropy.destinationPortValue});")
    Integer insertEntropy(@Param("tableName") String tableName,
                          @Param("entropy") Entropy entropy);

    /**
     * 根据表名新建 连接记录数据库
     * @param tableName
     */
    @Update("CREATE TABLE ${tableName} (\n" +
            "  `time` datetime NOT NULL COMMENT '时间点(秒)',\n" +
            "  `connectionNum` int DEFAULT NULL COMMENT '连接数',\n" +
            "  `sourceIpValue` float DEFAULT NULL COMMENT '源ip熵值',\n" +
            "  `destinationIpValue` float DEFAULT NULL COMMENT '目的ip熵值',\n" +
            "  `sourcePortValue` float NOT NULL COMMENT '源端口熵值',\n" +
            "  `destinationPortValue` float DEFAULT NULL COMMENT '目的端口熵值',\n" +
            "  PRIMARY KEY (`time`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;")
    void createTable(@Param("tableName") String tableName);

    /**
     * 根据表名删除表
     */
    @Update({"drop table if exists ${tableName}"})
    void dropExistTable(@Param("tableName") String tableName);
}
