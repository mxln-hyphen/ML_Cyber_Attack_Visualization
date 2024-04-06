package com.mxln.mlinteraction.mapper;


import Innercommon.datastructure.NewConnection;
import Innercommon.dojo.MySQLConnection;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DataMapper extends BaseMapper<NewConnection> {


    /**
     * 根据时间查询数据
     */
    @Select("SELECT * FROM ${tableName} where " +
            "(not lastPacketTimestamp <= '${startTime}') and (not firstPacketTimestamp>='${endTime}');")
    List<MySQLConnection> selectByTimeRange(@Param("tableName") String tableName,
                                          @Param("startTime") String startTime,
                                          @Param("endTime") String endTime);



}
