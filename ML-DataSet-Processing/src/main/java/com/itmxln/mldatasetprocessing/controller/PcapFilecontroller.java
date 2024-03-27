package com.itmxln.mldatasetprocessing.controller;

import com.itmxln.mldatasetprocessing.service.NewPackerReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PcapFilecontroller {

    @Autowired
    private NewPackerReader packerReader;

    @GetMapping("test")
    private String test(){
        return "test";
    }

    @GetMapping("loadfile")
    private String loadfile(){
        //String filename = "D:\\bishe\\pcap\\Monday-WorkingHours.pcap";
        String filename = "D:\\bishe\\pcap\\Tuesday-WorkingHours.pcap";

        packerReader.readPcapFile(filename);
        return "OK";
    }


}
