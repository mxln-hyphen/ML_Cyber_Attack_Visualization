package com.itmxln.mldatasetprocessing.elastic;

import Innercommon.datastructure.Connection;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ConnectionRepository extends ElasticsearchRepository<Connection, String> {
    // 在这里可以定义自定义查询方法
}
