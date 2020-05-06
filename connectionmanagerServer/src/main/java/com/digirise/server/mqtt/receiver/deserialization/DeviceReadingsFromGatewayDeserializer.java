package com.digirise.server.mqtt.receiver.deserialization;

import com.digirise.api.GatewayDataProtos;
import com.digirise.sai.commons.dataobjects.DeviceData;
import com.digirise.sai.commons.dataobjects.DeviceReading;
import com.digirise.sai.commons.dataobjects.DeviceReadingsFromGateway;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-04-27
 * Author: shrinkhlak
 */

@Component
public class DeviceReadingsFromGatewayDeserializer {
    public DeviceReadingsFromGateway deserializeDeviceReadingsFromGateway(
            GatewayDataProtos.DevicesReadingsFromGateway deviceReadingsProtobuf) {
        DeviceReadingsFromGateway deviceReadingsFromGateway = new DeviceReadingsFromGateway();
        List<DeviceData> deviceDataList = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;;
        LocalDateTime localDateTime = LocalDateTime.from(formatter.parse(deviceReadingsProtobuf.getGatewayTimestamp()));
        deviceReadingsFromGateway.setGatewayTimestamp(Timestamp.valueOf(localDateTime));

        for (GatewayDataProtos.DeviceData deviceDataProtobuf : deviceReadingsProtobuf.getDeviceDataList()) {
            DeviceData deviceData = new DeviceData();
            deviceData.setDeviceId(deviceDataProtobuf.getDeviceId());
            deviceData.setDeviceType(deviceDataProtobuf.getDeviceType());
            List<DeviceReading> readings = new ArrayList<>();
            for (GatewayDataProtos.DeviceReadings readingsProtobuf : deviceDataProtobuf.getAllReadingsFromDeviceList()) {
                DeviceReading reading = new DeviceReading();
                reading.setReadingType(readingsProtobuf.getReadingType());
                reading.setValue(readingsProtobuf.getValue());
                readings.add(reading);
            }
            deviceData.setDeviceReadings(readings);
            localDateTime = LocalDateTime.from(formatter.parse(deviceDataProtobuf.getTimestamp()));
            deviceData.setTimestamp(Timestamp.valueOf(localDateTime));
            deviceDataList.add(deviceData);
        }
        deviceReadingsFromGateway.setDeviceDataList(deviceDataList);
        return deviceReadingsFromGateway;
    }
}
