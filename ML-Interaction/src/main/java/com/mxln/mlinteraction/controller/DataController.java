package com.mxln.mlinteraction.controller;

import com.mxln.mlinteraction.response.ConnectionStatusResponse;
import com.mxln.mlinteraction.service.ElasticSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DataController {


    @Autowired
    private ElasticSearchService elasticSearchService;


    @GetMapping("/connections/timeRange")
    public List<ConnectionStatusResponse> getConnectionsByTimeRange(@RequestParam String startTime, @RequestParam String endTime) {
        System.out.println("开始时间:"+startTime);
        System.out.println("结束时间:"+endTime);
        // 调用 ElasticSearchService 服务层的方法
        return elasticSearchService.queryConnectionsByTimeRange(startTime, endTime);
    }
}
