package com.digirise.server.mqtt.receiver.deserialize;

import com.digirise.proto.CommnStructuresProtos;
import com.digirise.proto.GatewayDiscoveryProtos;
import com.digirise.sai.commons.discovery.DeviceInfo;
import com.digirise.sai.commons.discovery.GatewayDiscovery;
import com.digirise.sai.commons.helper.DeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-08
 * Author: shrinkhlak
 */

@Component
public class GatewayDiscoveryDeserializer {
    public static final Logger s_logger = LoggerFactory.getLogger(GatewayDiscoveryDeserializer.class);

    public GatewayDiscovery deserializeGatewayDiscovery(GatewayDiscoveryProtos.GatewayDiscovery gatewayDiscoveryProto) {
        GatewayDiscovery gatewayDiscovery = new GatewayDiscovery();
        gatewayDiscovery.setGatewayName(gatewayDiscoveryProto.getGatewayName());
        gatewayDiscovery.setCustomerName(gatewayDiscoveryProto.getCustomerName());
        gatewayDiscovery.setCustomerId(gatewayDiscoveryProto.getCustomerId());
        gatewayDiscovery.setLocation(gatewayDiscoveryProto.getLocation());
        gatewayDiscovery.setCoordinates(gatewayDiscoveryProto.getCoordinates());
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;;
        LocalDateTime localDateTime = LocalDateTime.from(formatter.parse(gatewayDiscoveryProto.getTimestamp()));
        gatewayDiscovery.setTimestamp(Timestamp.valueOf(localDateTime));
        if (gatewayDiscoveryProto.getDeviceInfosCount() > 0) {
            List<DeviceInfo> deviceInfoList = new ArrayList<>();
            gatewayDiscoveryProto.getDeviceInfosList().stream().forEach(s -> {
                    DeviceInfo deviceInfo = new DeviceInfo();
                    deviceInfo.setDeviceName(s.getDeviceName());
                    if (s.getDeviceType() == CommnStructuresProtos.DeviceType.TEMPERATURE_SENSOR)
                        deviceInfo.setDeviceType(DeviceType.TEMPERATURE_SENSOR);
                    else if (s.getDeviceType() == CommnStructuresProtos.DeviceType.MOTION_SENSOR)
                        deviceInfo.setDeviceType(DeviceType.MOTION_SENSOR);
                    else if (s.getDeviceType() == CommnStructuresProtos.DeviceType.HUMIDITY_SENSOR)
                        deviceInfo.setDeviceType(DeviceType.HUMIDITY_SENSOR);
                    deviceInfoList.add(deviceInfo);
                    });
            gatewayDiscovery.setDeviceIds(deviceInfoList);
        }
        return gatewayDiscovery;
    }
}
