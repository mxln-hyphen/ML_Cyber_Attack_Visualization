package com.itmxln.mldatasetprocessing;

import Innercommon.Enum.HTTPMethod;
import Innercommon.Enum.NewConnectionState;
import Innercommon.datastructure.NewConnection;
import com.itmxln.mldatasetprocessing.service.NewConnectionService;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.time.Instant;

@SpringBootTest
@RunWith(SpringRunner.class)
class MlDataSetProcessingApplicationTests {


    @Autowired
    private NewConnectionService newConnectionService;

    @Test
    void contextLoads() {
        NewConnection newConnection = new NewConnection(
               "192.168.0.1:80:100.100.100.100:443",
                "192.168.0.1",
                "100.100.100.100",
                80,
                442,
                NewConnectionState.CLOSED,
                false,
                Instant.ofEpochSecond(1711199228),
                Instant.ofEpochSecond(1711199237),
                9,
                10,
                20,
                true,
                HTTPMethod.GET,
                false,
                false
        );

        newConnectionService.writeMySQL("data_1",newConnection);

    }




}
