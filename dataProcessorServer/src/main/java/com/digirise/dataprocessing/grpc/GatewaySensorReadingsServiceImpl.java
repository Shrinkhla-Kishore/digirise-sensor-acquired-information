package com.digirise.dataprocessing.grpc;

import com.digirise.dataprocessing.database.SensorDataHandler;
import com.digirise.dataprocessing.deserializer.DeviceReadingsBetweenServersDeserializer;
import com.digirise.proto.CommnStructuresProtos;
import com.digirise.proto.GatewayDataForDpProtos;
import com.digirise.proto.GatewaySensorReadingsServiceGrpc;
import com.digirise.sai.commons.helper.DeviceReading;
import com.digirise.sai.commons.servercommunication.DeviceDataBetweenServers;
import com.digirise.sai.commons.servercommunication.DeviceReadingsBetweenServers;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-16
 * Author: shrinkhlak
 */

@Service
public class GatewaySensorReadingsServiceImpl extends GatewaySensorReadingsServiceGrpc.GatewaySensorReadingsServiceImplBase {
    public static final Logger s_logger = LoggerFactory.getLogger(GatewaySensorReadingsServiceImpl.class);
    @Autowired
    private DeviceReadingsBetweenServersDeserializer deviceReadingsBetweenServersDeserializer;
    @Autowired
    private SensorDataHandler sensorDataHandler;

    @Override
    public void gatewaySensorReadings(
            GatewayDataForDpProtos.DevicesReadingsBetweenServers devicesReadingsBetweenServersProto,
            StreamObserver<CommnStructuresProtos.DeviceReadingsResponse> responseObserver){

        s_logger.info("message received over gRPC is {}", devicesReadingsBetweenServersProto);
        s_logger.info("deserializer is {}, sensorDataHandler is {}", deviceReadingsBetweenServersDeserializer, sensorDataHandler);
        DeviceReadingsBetweenServers deviceReadingsBetweenServers = deviceReadingsBetweenServersDeserializer.deserializeDeviceReadings(devicesReadingsBetweenServersProto);
        s_logger.info("Gateway timestamp {}", deviceReadingsBetweenServers.getGatewayTimestamp().toString());
        for (DeviceDataBetweenServers deviceData : deviceReadingsBetweenServers.getDeviceDataList()) {
            s_logger.info("Device id {}", deviceData.getDeviceId());
            s_logger.info("device data timestamp {}", deviceData.getTimestamp().toString());
            for (DeviceReading reading : deviceData.getDeviceReadings()) {
                s_logger.info("reading type {}", reading.getReadingType().toString());
                s_logger.info("reading value: {}", reading.getValue());
                s_logger.info("unit: {}", reading.getUnit());
            }
        }
        // Storing the received data in InfluxDB
        boolean success = sensorDataHandler.createSensorMeasurement(deviceReadingsBetweenServers.getDeviceDataList(), deviceReadingsBetweenServers.getGatewayTimestamp());
//        String greeting = new StringBuilder()
//                .append("Hello, ")
//                .append(request.getFirstName())
//                .append(" ")
//                .append(request.getLastName())
//                .toString();

        sensorDataHandler.getSensorMeasurements();
        CommnStructuresProtos.ResponseStatus responseStatus;
        if (success)
            responseStatus = CommnStructuresProtos.ResponseStatus.SUCCESS;
        else
            responseStatus = CommnStructuresProtos.ResponseStatus.FAILED;

        CommnStructuresProtos.DeviceReadingsResponse responseProto = CommnStructuresProtos.DeviceReadingsResponse.newBuilder()
                .setStatus(responseStatus).build();

        responseObserver.onNext(responseProto);
        responseObserver.onCompleted();
    }
}
