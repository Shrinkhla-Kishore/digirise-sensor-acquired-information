package com.digirise.server.grpc.clientside;

import io.grpc.ConnectivityState;
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
        try {
            s_logger.info("Starting gRPC channel");
            managedChannel = ManagedChannelBuilder.forAddress(dataProcessingHost, Integer.parseInt(dataProcessingPort))
                    .usePlaintext().build();
            ConnectivityState connectivityState = managedChannel.getState(true);
            while (connectivityState == ConnectivityState.CONNECTING) {
                Thread.currentThread().sleep(2000);
                connectivityState = managedChannel.getState(true);
            }
            if (connectivityState == ConnectivityState.READY || connectivityState == ConnectivityState.IDLE)
                s_logger.info("gRPC channel started successfully. Connectivity State {}, Host {}, port {}:)",
                        connectivityState.name(), dataProcessingHost, dataProcessingPort);
            else
                s_logger.error("Error connecting to grpc dataprocessor server. Connectivity state {}", connectivityState.name());
        } catch (InterruptedException e) {
            managedChannel.shutdown();
            ConnectivityState connectivityState = managedChannel.getState(true);
            if (connectivityState != ConnectivityState.SHUTDOWN) {
                managedChannel.shutdownNow();
            }
        }
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
