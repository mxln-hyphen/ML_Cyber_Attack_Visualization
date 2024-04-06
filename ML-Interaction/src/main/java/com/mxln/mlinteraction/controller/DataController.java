package com.mxln.mlinteraction.controller;

import com.mxln.mlinteraction.response.ConnectionStatusResponse;
import com.mxln.mlinteraction.response.ConnectionsEntropyResponse;
import com.mxln.mlinteraction.service.ElasticSearchService;
import com.mxln.mlinteraction.service.MySQLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

@RestController
public class DataController {


    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private MySQLService mySQLService;

    // /**
    //  * 根据时间范围返回网络连接情况
    //  * @param tableName
    //  * @param startTime
    //  * @param endTime
    //  * @return 返回Json数组
    //  * @throws ParseException
    //  */
    // @GetMapping("/connections/timeRange")
    // public List<ConnectionStatusResponse> getConnectionsByTimeRange(@RequestParam String tableName,@RequestParam String startTime, @RequestParam String endTime) throws ParseException {
    //     System.out.println("开始时间:"+startTime);
    //     System.out.println("结束时间:"+endTime);
    //     // // 调用 ElasticSearchService 服务层的方法
    //     // return elasticSearchService.queryConnectionsByTimeRange(startTime, endTime);
    //
    //     // 调用 mysql 服务层的方法
    //     return mySQLService.queryConnectionsByTimeRange(tableName,startTime, endTime);
    // }

    /**
     * 根据时间范围返回网络连接情况
     * @param tableName
     * @param startTime
     * @param endTime
     * @return 返回数组
     * @throws ParseException
     */
    @GetMapping("/connections/timeRange")
    public Map<String,List<?>> getConnectionsByTimeRange(@RequestParam String tableName, @RequestParam String startTime, @RequestParam String endTime) throws ParseException {
        System.out.println("开始时间:"+startTime);
        System.out.println("结束时间:"+endTime);

        // 调用 mysql 服务层的方法
        return mySQLService.queryConnectionsByTimeRange(tableName,startTime, endTime,1);
    }



    @GetMapping("/test")
    public String test(){

        return "test";
    }

    /**
     * 根据时间范围查询熵值信息
     * @param tableName
     * @param startTime
     * @param endTime
     * @param granularity
     * @return
     * @throws ParseException
     */
    @GetMapping("/connections/entropy")
    public List<ConnectionsEntropyResponse> getConnectionsEntropyByTimeRange(@RequestParam String tableName,
                                                                             @RequestParam String startTime,
                                                                             @RequestParam String endTime,
                                                                             @RequestParam Integer granularity) throws ParseException {
        System.out.println("开始时间:"+startTime);
        System.out.println("结束时间:"+endTime);


        // 调用 mysql 服务层的方法
        return mySQLService.queryEntropyByTimeRange(tableName,startTime, endTime,granularity);
    }
}
