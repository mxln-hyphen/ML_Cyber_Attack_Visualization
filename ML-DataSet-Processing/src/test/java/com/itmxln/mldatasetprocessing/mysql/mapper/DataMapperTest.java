package com.itmxln.mldatasetprocessing.mysql.mapper;

import Innercommon.datastructure.Entropy;
import Innercommon.dojo.MySQLConnection;
import com.itmxln.mldatasetprocessing.service.EntropyService;
import com.itmxln.mldatasetprocessing.service.NewConnectionService;
import org.apache.ibatis.annotations.Param;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
class DataMapperTest {

    @Autowired
    private NewConnectionService newConnectionService;

    @Autowired
    private DataMapper dataMapper;

    @Autowired
    private EntropyMapper entropyMapper;

    @Autowired
    private EntropyService entropyService;

    @Test
    void deleteTemporaryConnectionByConnectionId() throws ParseException {

        //newConnectionService.deleteTemporaryConnectionByConnectionId("data_1", "192.168.0.1:80:100.100.100.100:443");
        // String filename = "D:\\bishe\\pcap\\Monday-WorkingHours.pcap";
        // String rex = "\\";
        // String substring = filename.substring(filename.lastIndexOf("\\") + 1, filename.lastIndexOf("."));
        // String[] split = substring.split("-");
        // String tableName = split[0] + "_" + split[1];
        //
        // dataMapper.dropExistTable(tableName);
        // dataMapper.createTable(tableName);


        // SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // simpleDateFormat.parse("2017-07-03 20:56:39");
        // List<MySQLConnection> entropy_1 = entropyMapper.selectConnectionBySecond("data_1",
        //         "2017-07-03 20:56:39");
        // System.out.println(entropy_1.size());
        //Entropy data_1 = entropyService.generateEntropyByTime("data_1", "2017-07-03 20:56:39");
        //System.out.println(data_1.toString());

        //entropyMapper.insertEntropy("entropy_1",data_1);
        entropyService.traverseByTime("tuesday_workinghours","2017-07-04 20:00:00","2017-07-05 4:00:00");


    }
}