package com.mxln.mlinteraction.response;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConnectionStatusResponse {
    private String sourceIp;
    private int sourcePort;
    private String time;
    private int destinationPort;
    private String destinationIp;
    private int status;


    public ConnectionStatusResponse(String sourceIp, int sourcePort, String time, int destinationPort, String destinationIp, int status) {
        this.sourceIp = sourceIp;
        this.sourcePort = sourcePort;
        this.time = time;
        this.destinationPort = destinationPort;
        this.destinationIp = destinationIp;
        this.status = status;
    }


}