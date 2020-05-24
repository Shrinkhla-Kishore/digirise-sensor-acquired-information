package com.digirise.gateway.mqtt.sender.serialization;

import com.digirise.proto.CommnStructuresProtos;
import com.digirise.proto.GatewayDiscoveryProtos;
import com.digirise.sai.commons.discovery.DeviceInfo;
import com.digirise.sai.commons.discovery.GatewayDiscovery;
import com.digirise.sai.commons.helper.DeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-07
 * Author: shrinkhlak
 */

@Component
public class GatewayDiscoverySerializer {
    public static final Logger s_logger = LoggerFactory.getLogger(GatewayDiscoverySerializer.class);

    public GatewayDiscoveryProtos.GatewayDiscovery serializeGatewayDiscovery(GatewayDiscovery gatewayDiscovery){
        GatewayDiscoveryProtos.GatewayDiscovery.Builder gatewayDiscoveryProto = GatewayDiscoveryProtos.GatewayDiscovery.newBuilder();
        gatewayDiscoveryProto.setGatewayName(gatewayDiscovery.getGatewayName());
        gatewayDiscoveryProto.setCustomerName(gatewayDiscovery.getCustomerName());
        gatewayDiscoveryProto.setCustomerId(gatewayDiscovery.getCustomerId());
        gatewayDiscoveryProto.setCoordinates(gatewayDiscovery.getCoordinates());
        gatewayDiscoveryProto.setLocation(gatewayDiscovery.getLocation());
        Timestamp timestamp = new Timestamp(new Date().getTime());
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        String timestampAsString = formatter.format(timestamp.toLocalDateTime());
        gatewayDiscoveryProto.setTimestamp(timestampAsString);

        List<GatewayDiscoveryProtos.DeviceInfo> devicesInfo = new ArrayList<>();
        if (gatewayDiscovery.getDeviceIds() != null){
            for (DeviceInfo deviceInfo : gatewayDiscovery.getDeviceIds()) {
                GatewayDiscoveryProtos.DeviceInfo.Builder deviceInfoProto = GatewayDiscoveryProtos.DeviceInfo.newBuilder();
                deviceInfoProto.setDeviceName(deviceInfo.getDeviceName());
                if (deviceInfo.getDeviceType() == DeviceType.MOTION_SENSOR)
                    deviceInfoProto.setDeviceType(CommnStructuresProtos.DeviceType.MOTION_SENSOR);
                else if (deviceInfo.getDeviceType() == DeviceType.HUMIDITY_SENSOR)
                    deviceInfoProto.setDeviceType(CommnStructuresProtos.DeviceType.HUMIDITY_SENSOR);
                else if (deviceInfo.getDeviceType() == DeviceType.TEMPERATURE_SENSOR)
                    deviceInfoProto.setDeviceType(CommnStructuresProtos.DeviceType.TEMPERATURE_SENSOR);
                devicesInfo.add(deviceInfoProto.build());
            }
        }
        return gatewayDiscoveryProto.build();
    }
}
