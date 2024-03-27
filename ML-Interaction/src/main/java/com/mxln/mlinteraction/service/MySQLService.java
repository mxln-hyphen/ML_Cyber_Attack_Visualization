package com.mxln.mlinteraction.service;

import Innercommon.Enum.HTTPMethod;
import Innercommon.Enum.NewConnectionState;
import Innercommon.datastructure.Entropy;
import Innercommon.datastructure.NewConnection;
import Innercommon.dojo.MySQLConnection;
import com.mxln.mlinteraction.mapper.DataMapper;
import com.mxln.mlinteraction.mapper.EntropyMapper;
import com.mxln.mlinteraction.response.ConnectionStatusResponse;
import com.mxln.mlinteraction.response.ConnectionsEntropyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


@Service
public class MySQLService {

    @Autowired
    private DataMapper dataMapper;

    @Autowired
    private EntropyMapper entropyMapper;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 根据时间范围查询网络连接情况
     */
    public List<ConnectionStatusResponse> queryConnectionsByTimeRange(String tableName, String startTime, String endTime) throws ParseException {
        //查询
        List<MySQLConnection> mySQLConnections = dataMapper.selectByTimeRange(tableName, startTime, endTime);
        List<ConnectionStatusResponse> ret = new LinkedList<>();

        //封装
        for (MySQLConnection mySQLConnection : mySQLConnections) {
            NewConnection newConnection = generateMySQLConnection(mySQLConnection);
            ret.add(generateConnectionStatusResponse(startTime, endTime, newConnection));
        }

        return ret;
    }

    /**
     * 根据时间范围查询熵值情况，每个时间段长度为granularity
     *
     * @param tableName
     * @param startTime
     * @param endTime
     * @param granularity
     * @return
     */
    public List<ConnectionsEntropyResponse> queryEntropyByTimeRange(String tableName, String startTime
            , String endTime, int granularity) throws ParseException {
        Instant now = sdf.parse(startTime).toInstant();
        Instant finalTime = sdf.parse(endTime).toInstant();

        List<ConnectionsEntropyResponse> list = new LinkedList<>();

        //遍历时间段
        while (now.isBefore(finalTime)) {
            Instant next = now.plusSeconds(granularity);
            //查询
            List<Entropy> entropies = entropyMapper.selectEntropyByTimeRange(tableName
                    , sdf.format(new Date(now.toEpochMilli()))
                    , sdf.format(new Date(next.toEpochMilli())));
            //汇总时间段内时间
            ConnectionsEntropyResponse e = generateConnectionsEntropyResponse(entropies);
            if(e!=null)
                list.add(e);
            //下一个时间段
            now = next;
        }

        return list;
    }

    /**
     * 将一个时间段内的数据汇总成一个值
     *
     * @return
     */
    private ConnectionsEntropyResponse generateConnectionsEntropyResponse(List<Entropy> entropies) {
        ConnectionsEntropyResponse connectionsEntropyResponse = new ConnectionsEntropyResponse();
        if(entropies.size()==0){
            return null;
        }
        connectionsEntropyResponse.setTime(sdf.format(entropies.get(0).getTime()));

        int connectionNum = 0;//连接数
        double sourceIpValue = 0.0; //源ip熵值
        double destinationIpValue = 0.0; //目的ip熵值
        double sourcePortValue = 0.0; //源端口熵值
        double destinationPortValue = 0.0; //目的端口熵值

        for (Entropy entropy : entropies) {
            connectionNum += entropy.getConnectionNum();
            sourceIpValue += entropy.getSourceIpValue();
            sourcePortValue += entropy.getSourcePortValue();
            destinationIpValue += entropy.getDestinationIpValue();
            destinationPortValue += entropy.getDestinationPortValue();
        }

        //计算平均数
        sourceIpValue /= entropies.size();
        sourcePortValue /= entropies.size();
        destinationIpValue /= entropies.size();
        destinationPortValue /= entropies.size();

        //设置返回值
        connectionsEntropyResponse.setConnectionNum(connectionNum);
        connectionsEntropyResponse.setSourceIpValue(sourceIpValue);
        connectionsEntropyResponse.setSourcePortValue(sourcePortValue);
        connectionsEntropyResponse.setDestinationIpValue(destinationIpValue);
        connectionsEntropyResponse.setDestinationPortValue(destinationPortValue);

        return connectionsEntropyResponse;
    }

    /**
     * 将NewConnection类转换为ConnectionStatusResponse类
     */
    private ConnectionStatusResponse generateConnectionStatusResponse(String lower, String upper, NewConnection newConnection) throws ParseException {
        //确定一个准确时间，不能超过查询边界。
        Instant startTime = newConnection.getFirstPacketTimestamp();
        Instant endTime = newConnection.getLastPacketTimestamp();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Instant start = sdf.parse(lower).toInstant();
        Instant end = sdf.parse(upper).toInstant();

        Instant finalTime;
        if (startTime.isAfter(start) && endTime.isBefore(end)) {
            finalTime = startTime;
        } else if (startTime.isAfter(start)) {//开始时间在范围内
            finalTime = start;
        } else if (endTime.isBefore(end)) {//结束时间在范围内
            finalTime = end;
        } else {//都不在
            finalTime = startTime;
        }

        ConnectionStatusResponse connectionStatusResponse = new ConnectionStatusResponse(
                newConnection.getSourceIpAddress(),
                newConnection.getSourcePort(),
                sdf.format(new Date(finalTime.toEpochMilli())),
                newConnection.getDestinationPort(),
                newConnection.getDestinationIpAddress(),
                newConnection.getState().ordinal()
        );

        return connectionStatusResponse;
    }

    /**
     * 把从数据库查询到的MySQLConnection类转化为NewConnection
     */
    NewConnection generateMySQLConnection(MySQLConnection mySQLConnection) {
        NewConnection newConnection = new NewConnection();
        newConnection.setConnectionId(mySQLConnection.getConnectionId());
        newConnection.setSourceIpAddress(mySQLConnection.getSourceIpAddress());
        newConnection.setDestinationIpAddress(mySQLConnection.getDestinationIpAddress());
        newConnection.setSourcePort(mySQLConnection.getSourcePort());
        newConnection.setDestinationPort(mySQLConnection.getDestinationPort());
        newConnection.setState(NewConnectionState.codeOf(mySQLConnection.getState()));
        newConnection.setIsOver(mySQLConnection.getIsOver());
        newConnection.setFirstPacketTimestamp(Instant.ofEpochMilli(mySQLConnection.getFirstPacketTimestamp().getTime()));
        newConnection.setLastPacketTimestamp(Instant.ofEpochMilli(mySQLConnection.getLastPacketTimestamp().getTime()));
        newConnection.setConnectionDuration(newConnection.getConnectionDuration());
        newConnection.setForwardPacketCount(newConnection.getForwardPacketCount());
        newConnection.setBackwardPacketCount(newConnection.getBackwardPacketCount());
        newConnection.setIsHTTP(newConnection.getIsHTTP());
        newConnection.setHttpMethod(HTTPMethod.codeOf(mySQLConnection.getHttpMethod()));
        newConnection.setHttpIsSuccess(mySQLConnection.getHttpIsSuccess());
        newConnection.setIsTemporary(mySQLConnection.getIsTemporary());
        newConnection.setEx2(mySQLConnection.getEx2());
        newConnection.setEx3(mySQLConnection.getEx3());
        newConnection.setEx4(mySQLConnection.getEx4());

        return newConnection;
    }


}
