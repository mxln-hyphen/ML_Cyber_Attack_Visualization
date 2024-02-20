package com.itmxln.mldatasetprocessing;

import com.itmxln.mldatasetprocessing.dataprocessing.PacketReader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class MlDataSetProcessingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MlDataSetProcessingApplication.class, args);
    }

    @Bean
    CommandLineRunner run(PacketReader packetReader) {
        return args -> {
            String pcapFilePath = "D:\\bishe\\pcap\\Monday-WorkingHours.pcap";
            packetReader.readPcapFile(pcapFilePath);
        };
    }

}
