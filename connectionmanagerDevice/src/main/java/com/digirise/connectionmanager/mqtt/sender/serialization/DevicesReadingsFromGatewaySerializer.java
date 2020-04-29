package com.digirise.connectionmanager.mqtt.sender.serialization;

import com.digirise.api.GatewayDataProtos;
import com.digirise.sai.commons.dataobjects.DeviceData;
import com.digirise.sai.commons.dataobjects.DeviceReading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-04-24
 * Author: shrinkhlak
 */

@Component
public class DevicesReadingsFromGatewaySerializer {
    private static final Logger s_logger = LoggerFactory.getLogger(DevicesReadingsFromGatewaySerializer.class);

    public GatewayDataProtos.DevicesReadingsFromGateway serializeDevicesData(List<DeviceData> devicesData){
        s_logger.info("Serializing gatewayReadings ... connected number of devices {}", devicesData.size());
        GatewayDataProtos.DevicesReadingsFromGateway.Builder devicesReadingsFromGateway =
                GatewayDataProtos.DevicesReadingsFromGateway.newBuilder();
        Timestamp timestamp = new Timestamp(new Date().getTime());
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        String timestampAsString = formatter.format(timestamp.toLocalDateTime());
        devicesReadingsFromGateway.setGatewayTimestamp(timestampAsString);
        s_logger.info("Setting gateway timestamp to {} inside protobuf message", timestampAsString);
        for (DeviceData deviceData : devicesData) {
            devicesReadingsFromGateway.addDeviceData(serializeDeviceData(deviceData));
        }
        return devicesReadingsFromGateway.build();
    }

    private GatewayDataProtos.DeviceData serializeDeviceData(DeviceData deviceDataToSend) {
        s_logger.info("Serializing device data ...");
        GatewayDataProtos.DeviceData.Builder deviceDataBuilder = GatewayDataProtos.DeviceData.newBuilder();
        deviceDataBuilder.setDeviceId(deviceDataToSend.getDeviceId());
        deviceDataBuilder.setDeviceType(deviceDataToSend.getDeviceType());
        s_logger.info("building protobuf for device id {} : {}.",
                deviceDataBuilder.getDeviceId(), deviceDataBuilder.getDeviceType());
        if (deviceDataToSend.getDeviceReadings() != null && deviceDataToSend.getDeviceReadings().size() > 0) {
            s_logger.info("Size of device readings is {}", deviceDataToSend.getDeviceReadings().size());
        } else {
            s_logger.info("The device does not contain any device readings :(");
        }

        for (DeviceReading deviceReading : deviceDataToSend.getDeviceReadings()) {
            GatewayDataProtos.ReadingType readingType = deviceReading.getReadingType();
            String value = deviceReading.getValue();
            GatewayDataProtos.DeviceReadings.Builder readings = GatewayDataProtos.DeviceReadings.newBuilder();
            readings.setReadingType(readingType);
            readings.setValue(value);
            deviceDataBuilder.addAllReadingsFromDevice(readings.build());
            s_logger.info("Readings put in device is {}, {}", readingType.toString(), value);
        }
        if (deviceDataToSend.getTimestamp() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            String timestampAsString = formatter.format(deviceDataToSend.getTimestamp().toLocalDateTime());
            deviceDataBuilder.setTimestamp(timestampAsString);
            s_logger.info("device's timestamp is {}", timestampAsString);
        }
        return deviceDataBuilder.build();
    }

}
