package com.digirise.server.grpc.serverside;

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

/** GrpcServer class builds and starts the GRPC server
 * Created by IntelliJ IDEA.
 * Date: 2021-01-06
 * Author: shrinkhlak
 */
@Component
public class GrpcServer implements Runnable {
    public static final Logger s_logger = LoggerFactory.getLogger(GrpcServer.class);
    private Server grpcServer;
    @Value("${grpc.server.port}")
    private String grpcPort;
    @Autowired
    private CustomersInformationServiceImpl customersInformationService;
    @Autowired
    private GatewaysInformationServiceImpl gatewaysInformationService;
    @Autowired
    private SensorsInformationServiceImpl sensorsInformationService;
    private static ExecutorService grpcExecutor = Executors.newSingleThreadExecutor();

    @PostConstruct
    public void setUpGrpCServer() {
        s_logger.info("Staring gRPC server for handling frontend requests on port {}", grpcPort);
        grpcExecutor.submit(this);
    }

    @Override
    public void run() {
        try {
                s_logger.info("Staring gRPC on port {} for handling requests from the data processor", grpcPort);
            grpcServer = ServerBuilder.forPort(Integer.parseInt(grpcPort)).addService(customersInformationService)
                    .addService(gatewaysInformationService).addService(sensorsInformationService).build();
            grpcServer.start();
            grpcServer.awaitTermination();
            if (!grpcServer.isShutdown() && !grpcServer.isTerminated())
                s_logger.info("gRPC server started successfully");
        } catch (IOException e) {
        } catch (InterruptedException e){
            grpcServer.shutdownNow();
        }
    }

}
