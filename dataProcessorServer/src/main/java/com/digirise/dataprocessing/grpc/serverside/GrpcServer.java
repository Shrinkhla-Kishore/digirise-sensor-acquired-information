package com.digirise.dataprocessing.grpc.serverside;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * GrpcServer class is server side for handling all the gateway data readings that is sent by the
 * backend server for storing it in Influx DB.
 *
 * Created by IntelliJ IDEA.
 * Date: 2020-05-15
 * Author: shrinkhlak
 */

@Component
public class GrpcServer implements Runnable {
    public static final Logger s_logger = LoggerFactory.getLogger(GrpcServer.class);
    private Server grpcServer;
    @Value("${server.grpc.port}")
    private int grpcPort;
    @Autowired
    private GatewaySensorReadingsServiceImpl gatewaySensorReadingsService;
    private static ExecutorService grpcExecutor = Executors.newSingleThreadExecutor();

    @PostConstruct
    public void setUpGrpCServer(){
        s_logger.info("Staring gRPC on port {}", grpcPort);
        grpcExecutor.submit(this);
    }

    @Override
    public void run() {
        try {
            s_logger.info("Staring gRPC on Port {}, GatewaySensorReadingsServiceImpl is {} ", grpcPort, gatewaySensorReadingsService);
            grpcServer = ServerBuilder.forPort(grpcPort).addService(gatewaySensorReadingsService).build();
            grpcServer.start();
            grpcServer.awaitTermination();
            s_logger.info("gRPC server started successfully");
        } catch (IOException e) {
            s_logger.warn(e.getMessage());
        } catch (InterruptedException e){
            s_logger.trace(e.getMessage());
        }
    }
}
