package com.digirise.server.handler;

import com.digirise.proto.GatewayDataProtos;
import com.digirise.proto.GatewaySensorReadingsServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-13
 * Author: shrinkhlak
 */

@Service
public class DataProcessingGrpcClient {
    public static final Logger s_logger = LoggerFactory.getLogger(DataProcessingGrpcClient.class);
    @Value("${dataprocessing.host}")
    private String dataProcessingHost;
    @Value("${dataprocessing.grpc.port}")
    private String dataProcessingPort;
    private ManagedChannel managedChannel;

    @PostConstruct
    public void setUpGrpcHandler() {
        s_logger.info("Starting gRPC channel");
        managedChannel = ManagedChannelBuilder.forAddress(dataProcessingHost, Integer.parseInt(dataProcessingPort))
                .usePlaintext().build();
        s_logger.info("gRPC channel started successfully. Host {}, port {}:)", dataProcessingHost, dataProcessingPort);
    }

    @PreDestroy
    public void stopGrpcHandler() {
        s_logger.info("Stopping the Grpc channel");
        managedChannel.shutdown();
    }

    public ManagedChannel getManagedChannel() {
        return managedChannel;
    }
}
