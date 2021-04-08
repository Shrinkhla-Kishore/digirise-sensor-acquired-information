package com.digirise.server.grpc.serverside;

import com.digirise.proto.CommonStructuresProto;
import com.digirise.proto.frontendmessages.SensorsInfoProto;
import com.digirise.proto.frontendmessages.SensorsInformationServiceGrpc;
import com.digirise.sai.commons.helper.DeviceType;
import com.digirise.server.model.Gateway;
import com.digirise.server.model.GatewayRepository;
import com.digirise.server.model.Sensor;
import com.digirise.server.model.SensorRepository;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * Date: 2021-01-06
 * Author: shrinkhlak
 */

@Component
public class SensorsInformationServiceImpl
        extends SensorsInformationServiceGrpc.SensorsInformationServiceImplBase {
private static final Logger s_logger = LoggerFactory.getLogger(SensorsInformationServiceImpl.class);
    @Autowired
    private SensorRepository sensorRepository;
    @Autowired
    private GatewayRepository gatewayRepository;

    @Override
    @Transactional
    public void getSensorsInformation(SensorsInfoProto.SensorsInfoRequest sensorsInfoRequest,
                                      StreamObserver<SensorsInfoProto.SensorsInfoResponse> sensorsInfoResponseObserver){
        //Service logic
        SensorsInfoProto.SensorsInfoResponse.Builder sensorsInfoRespBuilder =
                SensorsInfoProto.SensorsInfoResponse.newBuilder();
        SensorsInfoProto.SensorInfo.Builder sensorInfoBuilder = SensorsInfoProto.SensorInfo.newBuilder();
        Optional<Gateway> gatewayFromDb = gatewayRepository.findById(sensorsInfoRequest.getGatewayId());
        if (gatewayFromDb.isPresent()) {
            Stream<Sensor> sensorsStream = sensorRepository.findAllSensorByGatewayId(gatewayFromDb.get());
            sensorsStream.forEach((sensor) -> {
                sensorInfoBuilder.setSensorId(sensor.getSensorId());
                sensorInfoBuilder.setName(sensor.getSensorName());
                if (sensor.getLocation() != null)
                    sensorInfoBuilder.setLocation(sensor.getLocation());
                if (sensor.getType() != null) {
                    CommonStructuresProto.DeviceType deviceType = null;
                    if (sensor.getType() == DeviceType.MOTION_SENSOR)
                        deviceType = CommonStructuresProto.DeviceType.MOTION_SENSOR;
                    else if (sensor.getType() == DeviceType.TEMPERATURE_SENSOR)
                        deviceType = CommonStructuresProto.DeviceType.TEMPERATURE_SENSOR;
                    else if (sensor.getType() == DeviceType.HUMIDITY_SENSOR)
                        deviceType = CommonStructuresProto.DeviceType.HUMIDITY_SENSOR;
                    else if (sensor.getType() == DeviceType.LIGHT_SENSOR)
                        deviceType = CommonStructuresProto.DeviceType.LIGHT_SENSOR;
                    sensorInfoBuilder.setDeviceType(deviceType);
                }
                if (sensor.getCreatedOn() != null)
                    sensorInfoBuilder.setCreatedOn(sensor.getCreatedOn().toString());
                sensorsInfoRespBuilder.addSensorInfo(sensorInfoBuilder.build());
            });
        } else {
            s_logger.warn("Unable to find any sensors for the gatewayId {} as gateway not found", sensorsInfoRequest.getGatewayId());
        }
        sensorsInfoResponseObserver.onNext(sensorsInfoRespBuilder.build());
        sensorsInfoResponseObserver.onCompleted();
    }
}