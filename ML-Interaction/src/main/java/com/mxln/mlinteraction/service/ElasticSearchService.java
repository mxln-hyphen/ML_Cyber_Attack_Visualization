package com.mxln.mlinteraction.service;

import com.mxln.mlinteraction.response.ConnectionStatusResponse;
import com.mxln.mlinteraction.utils.ElasticUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ElasticSearchService {

    @Autowired
    private ElasticUtil elasticUtil;

    public List<ConnectionStatusResponse> queryConnectionsByTimeRange(String startTime, String endTime) {
        // 调用 ElasticUtil 工具类的方法
        List<ConnectionStatusResponse> connectionStatusResponses = elasticUtil.queryConnectionsByTimeRange(startTime, endTime);
        return connectionStatusResponses;
    }
}
