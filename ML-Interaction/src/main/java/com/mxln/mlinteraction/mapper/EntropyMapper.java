package com.mxln.mlinteraction.mapper;

import Innercommon.datastructure.Entropy;
import Innercommon.dojo.MySQLConnection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EntropyMapper {


    /**
     * 根据时间查询熵值数据
     *
     * @param tableName
     * @return
     */
    @Select("SELECT * FROM ${tableName}  WHERE time >= '${startTime}' AND time <= '${endTime}';")
    List<Entropy> selectEntropyByTimeRange(@Param("tableName") String tableName,
                                           @Param("startTime") String startTime,
                                           @Param("endTime") String endTime);


}
