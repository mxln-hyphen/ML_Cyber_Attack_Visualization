package com.mxln.mlinteraction.response;

import lombok.Data;

import java.util.Date;

@Data
public class ConnectionsEntropyResponse {

    private String time;  //时间

    private int connectionNum; //网络连接数

    private double sourceIpValue; //源ip熵值

    private double destinationIpValue; //目的ip熵值

    private double sourcePortValue; //源端口熵值

    private double destinationPortValue; //目的端口熵值
}
