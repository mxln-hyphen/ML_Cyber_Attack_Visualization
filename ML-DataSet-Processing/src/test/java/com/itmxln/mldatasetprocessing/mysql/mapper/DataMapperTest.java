package com.itmxln.mldatasetprocessing.mysql.mapper;

import com.itmxln.mldatasetprocessing.service.NewConnectionService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@RunWith(SpringRunner.class)
class DataMapperTest {

    @Autowired
    private NewConnectionService newConnectionService;

    @Test
    void deleteTemporaryConnectionByConnectionId() {

        newConnectionService.deleteTemporaryConnectionByConnectionId("data_1","192.168.0.1:80:100.100.100.100:443");


    }
}