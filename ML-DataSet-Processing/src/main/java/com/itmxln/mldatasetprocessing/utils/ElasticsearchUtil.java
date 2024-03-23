package com.itmxln.mldatasetprocessing.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import Innercommon.datastructure.Connection;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.apache.http.HttpHost;
import org.springframework.beans.factory.annotation.Value;

public class ElasticsearchUtil {

    private RestHighLevelClient client;
    private ObjectMapper objectMapper;

    @Value("${elasticsearch.host}")
    private String elasticsearchHost;

    @Value("${elasticsearch.port}")
    private int elasticsearchPort;

    public ElasticsearchUtil() {
        // 初始化Elasticsearch客户端
        client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(elasticsearchHost, elasticsearchPort, "http")));
        objectMapper = new ObjectMapper();
    }

    public void write(Connection connection) {
        try {
            // 将Connection对象转换为JSON字符串
            String json = objectMapper.writeValueAsString(connection);
            // 创建一个IndexRequest，准备将数据写入Elasticsearch
            IndexRequest indexRequest = new IndexRequest("connections")
                    .source(json, XContentType.JSON);
            // 执行写入操作
            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        // 关闭Elasticsearch客户端
        try {
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
