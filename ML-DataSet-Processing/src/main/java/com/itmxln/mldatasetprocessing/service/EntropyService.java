package com.itmxln.mldatasetprocessing.service;

import Innercommon.datastructure.Entropy;
import Innercommon.dojo.MySQLConnection;
import com.itmxln.mldatasetprocessing.mysql.mapper.EntropyMapper;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 提供熵相关的计算
 */
@Service
public class EntropyService {

    @Autowired
    private EntropyMapper entropyMapper;


    /**
     * 根据开始时间和结束时间获取所有时间点的熵值,并写入数据库
     */
    public void traverseByTime(String tableName, String startTime, String endTime) throws ParseException {
        //目标表名
        String EntropyTableName = tableName+"_entropy";
        //创建表
        createTable(EntropyTableName);

        //算一下要多久
        long l = System.currentTimeMillis();
        int i=0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Instant start = sdf.parse(startTime).toInstant();
        Instant end = sdf.parse(endTime).toInstant();
        //从开始时间遍历到结束时间
        while (start.isBefore(end)) {
            try {
                //查询并计算熵值
                Entropy entropy = generateEntropyByTime(tableName, new Date(start.toEpochMilli()));
                entropyMapper.insertEntropy(EntropyTableName, entropy);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            start = start.plusSeconds(1);
            i++;
            if(i%1800==0) {
                System.out.print("Time: " + sdf.format(new Date(start.toEpochMilli())) + "\n"); // 打印计数器的当前值
            }
        }

        System.out.println(System.currentTimeMillis()-l);
    }


    /**
     * 在数据库中查询对应时间的所有熵值
     */
    public Entropy generateEntropyByTime(String tableName, Date time) throws ParseException {
        //以后可能要处理一下时间字符串格式

        //查询
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = dateFormat.format(time);
        List<MySQLConnection> mySQLConnections = entropyMapper.selectConnectionBySecond(tableName, format);

        //新建entropy并设置时间
        Entropy entropy = new Entropy();
        //entropy.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time));
        entropy.setTime(time);

        //设计连接数
        int size = mySQLConnections.size();
        entropy.setConnectionNum(size);

        //统计Map
        HashMap<String, Integer> sourceIpMap = new HashMap<>();
        HashMap<String, Integer> destinationIpMap = new HashMap<>();
        HashMap<String, Integer> sourcePortMap = new HashMap<>();
        HashMap<String, Integer> destinationPortMap = new HashMap<>();

        //统计
        for (MySQLConnection mySQLConnection : mySQLConnections) {
            mapAdd(sourceIpMap, mySQLConnection.getSourceIpAddress());
            mapAdd(destinationIpMap, mySQLConnection.getDestinationIpAddress());
            mapAdd(sourcePortMap, String.valueOf(mySQLConnection.getSourcePort()));
            mapAdd(destinationPortMap, String.valueOf(mySQLConnection.getDestinationPort()));
        }

        //计算熵值
        entropy.setSourceIpValue(countEntropy(sourceIpMap, size));
        entropy.setDestinationIpValue(countEntropy(destinationIpMap, size));
        entropy.setSourcePortValue(countEntropy(sourcePortMap, size));
        entropy.setDestinationPortValue(countEntropy(destinationPortMap, size));


        return entropy;
    }

    /**
     * 把记录添加到map中
     */
    private void mapAdd(HashMap<String, Integer> map, String key) {
        if (map.containsKey(key)) {
            map.put(key, map.get(key) + 1);
        } else {
            map.put(key, 1);
        }
    }

    /**
     * 根据map计算熵值
     *
     * @return
     */
    private double countEntropy(HashMap<String, Integer> map, int sum) {
        float entropy = 0;

        //计算熵值
        for (Integer value : map.values()) {
            double p = (double) value / sum;
            entropy += (-(p * Math.log(p) / Math.log(2)));
        }

        //把熵值控制在0~1内
        double ret = entropy / ((Math.log(sum) / Math.log(2)));
        ret = ret > 1 ? 1 : ret;
        return ret;
    }

    /**
     * 根据表名创建对应表
     */
    private String createTable(String tableName){
        //如果已经存在，删除表
        entropyMapper.dropExistTable(tableName);

        //创建表
        entropyMapper.createTable(tableName);
        return tableName;
    }


}
