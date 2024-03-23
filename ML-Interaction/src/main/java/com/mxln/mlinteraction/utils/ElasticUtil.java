package com.mxln.mlinteraction.utils;

import Innercommon.Enum.ConnectionState;
import Innercommon.datastructure.Connection;
import com.mxln.mlinteraction.response.ConnectionStatusResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ElasticUtil {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    public List<ConnectionStatusResponse> queryConnectionsByTimeRange(String startTime, String endTime) {
        // 构建布尔查询构造器，用于组合查询条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("firstPacketTimestamp") // 创建一个范围查询
                        .gte(startTime) // 开始时间（大于等于）
                        .lte(endTime)); // 结束时间（小于等于）

        // 构建原生搜索查询对象，设置查询构造器为之前创建的布尔查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .build();

        // 执行查询并获取结果，SearchHits<Connection> 封装了查询命中的所有记录
        SearchHits<Connection> searchHits = elasticsearchRestTemplate.search(searchQuery, Connection.class);

        // 将查询结果（SearchHits）转换为 ConnectionStatusResponse 列表
        // 使用 Java Stream API 进行转换
        return searchHits.getSearchHits().stream().map(hit -> {
            Connection connection = hit.getContent(); // 获取每个命中的文档内容，即 Connection 对象
            String status = connection.getState()== ConnectionState.FINISHED ? "success" : "fail"; // 根据状态字段确定状态值
            // System.out.println(connection.getState()+" "+connection.getState().getClass());
            return new ConnectionStatusResponse(
                    connection.getSourceIpAddress(), // 源 IP 地址
                    connection.getSourcePort(), // 源端口
                    connection.getFirstPacketTimestamp().toString(), // 第一个数据包的时间戳，可能需要格式化
                    connection.getDestinationPort(), // 目的端口
                    connection.getDestinationIpAddress(), // 目的 IP 地址
                    status); // 连接状态（成功或失败）
        }).collect(Collectors.toList()); // 收集处理结果为 List
    }
}