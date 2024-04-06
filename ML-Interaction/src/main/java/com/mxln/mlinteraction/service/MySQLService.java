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
import java.time.temporal.ChronoUnit;
import java.util.*;


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
     * 根据时间范围查询网络连接情况,模式2，返回一个数组
     */
    public Map<String, List<?>> queryConnectionsByTimeRange(String tableName, String startTime, String endTime,
                                                            int method) throws ParseException {
        //查询
        List<MySQLConnection> mySQLConnections = dataMapper.selectByTimeRange(tableName, startTime, endTime);
        Map<String, List<?>> ret = new HashMap<>();

        //
        List<String> sourceIpList = new LinkedList<>();
        List<Integer> sourcePortList = new LinkedList<>();
        List<String> timeList = new LinkedList<>();
        List<Integer> dstPortList = new LinkedList<>();
        List<String> dstIpList = new LinkedList<>();
        List<Integer> stateList = new LinkedList<>();

        //封装
        for (MySQLConnection mySQLConnection : mySQLConnections) {
            sourceIpList.add(mySQLConnection.getSourceIpAddress());
            sourcePortList.add(mySQLConnection.getSourcePort());
            timeList.add(sdf.format(new Date(getFinalTime(
                    startTime,
                    endTime,
                    mySQLConnection.getFirstPacketTimestamp().toInstant()
                    , mySQLConnection.getLastPacketTimestamp().toInstant()
            )
                    .toEpochMilli()
            )));
            dstPortList.add(mySQLConnection.getDestinationPort());
            dstIpList.add(mySQLConnection.getDestinationIpAddress());
            stateList.add(getRetState(mySQLConnection.getState()));
        }

        //将列表放进Map
        ret.put("SourceIp", sourceIpList);
        ret.put("SourcePort", sourcePortList);
        ret.put("Time", timeList);
        ret.put("DestinationPort", dstPortList);
        ret.put("DestinationIp", dstIpList);
        ret.put("State", stateList);

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
            if (e != null)
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
        if (entropies.size() == 0) {
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
        Instant finalTime = getFinalTime(lower, upper, newConnection.getFirstPacketTimestamp(), newConnection.getLastPacketTimestamp());

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
     * 根据连接的开始和结束时间，以及查询范围选择一个合适的时间点来表示这条连接
     *
     * @param lower
     * @param upper
     * @return
     * @throws ParseException
     */
    private Instant getFinalTime(String lower, String upper, Instant startTime, Instant endTime) throws ParseException {
        Instant start = sdf.parse(lower).toInstant();
        Instant end = sdf.parse(upper).toInstant();

        Instant finalTime = null;

        //四种情况
        if (startTime.compareTo(start) >= 0 && endTime.compareTo(end) <= 0) {//连接完全包含在范围中
            //时间点是开始时间和结束时间中点
            finalTime = getMid(startTime, endTime);
        } else if (startTime.compareTo(start) <= 0 && endTime.compareTo(end) >= 0) {//连接包含整个范围
            //时间点是上界和下界的中点
            finalTime = getMid(start, end);
        } else if (startTime.compareTo(start) <= 0 && endTime.compareTo(start) >= 0) {//范围上界被包含在连接中
            //时间点是上界和结束时间的中点
            finalTime = getMid(start, endTime);
        } else if (startTime.compareTo(end) <= 0 && endTime.compareTo(end) >= 0) {//范围下界被包含在连接中
            //时间点是开始时间和下界的中点
            finalTime = getMid(startTime, end);
        } else {
            System.out.println(1);
        }

        return finalTime;
    }

    /**
     * 返回两个时间点的中点
     *
     * @param start
     * @param end
     * @return
     */
    private Instant getMid(Instant start, Instant end) {
        //时间差值的一半
        long between = ChronoUnit.MILLIS.between(start, end) / 2;
        //开始时间加差值一半也就是两个时间的中点
        return start.plusMillis(between);
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

    /**
     * 获取返回给前端的值
     *
     * @param state
     * @return
     */
    int getRetState(Integer state) {
        switch (state) {
            case 6:
            case 7:
                return 1;
        }

        return 0;
    }


}
