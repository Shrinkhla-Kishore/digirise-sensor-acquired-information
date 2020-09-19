package com.digirise.gateway.mqtt.sender.serialization;

import com.digirise.proto.CommnStructuresProtos;
import com.digirise.proto.GatewayDataProtos;
import com.digirise.sai.commons.helper.DeviceReading;
import com.digirise.sai.commons.helper.ReadingType;
import com.digirise.sai.commons.readings.DeviceData;
import com.digirise.sai.commons.readings.DeviceReadingsFromGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-04-24
 * Author: shrinkhlak
 */

@Component
public class DevicesReadingsFromGatewaySerializer {
    private static final Logger s_logger = LoggerFactory.getLogger(DevicesReadingsFromGatewaySerializer.class);

    public GatewayDataProtos.DevicesReadingsFromGateway serializeDevicesData(DeviceReadingsFromGateway devicesReadingsFromGateway){
        List<DeviceData> devicesData = devicesReadingsFromGateway.getDeviceDataList();
        s_logger.info("Serializing gatewayReadings ... connected number of devices {}", devicesData.size());
        GatewayDataProtos.DevicesReadingsFromGateway.Builder devicesReadingsFromGatewayProto =
                GatewayDataProtos.DevicesReadingsFromGateway.newBuilder();
        Timestamp timestamp = devicesReadingsFromGateway.getGatewayTimestamp();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        String timestampAsString = formatter.format(timestamp.toLocalDateTime());
        devicesReadingsFromGatewayProto.setGatewayTimestamp(timestampAsString);
        devicesReadingsFromGatewayProto.setGatewayName(devicesReadingsFromGateway.getGatewayName());
        devicesReadingsFromGatewayProto.setCustomerName(devicesReadingsFromGateway.getCustomerName());
        s_logger.info("Setting gateway timestamp to {} inside protobuf message", timestampAsString);
        for (DeviceData deviceData : devicesData) {
            devicesReadingsFromGatewayProto.addDeviceData(serializeDeviceData(deviceData));
        }
        return devicesReadingsFromGatewayProto.build();
    }

    private GatewayDataProtos.DeviceData serializeDeviceData(DeviceData deviceDataToSend) {
        s_logger.info("Serializing device data ...");
        GatewayDataProtos.DeviceData.Builder deviceDataBuilder = GatewayDataProtos.DeviceData.newBuilder();
        deviceDataBuilder.setDeviceName(deviceDataToSend.getDeviceName());
        s_logger.info("building protobuf for device id {}.", deviceDataBuilder.getDeviceName());
        if (deviceDataToSend.getDeviceReadings() != null && deviceDataToSend.getDeviceReadings().size() > 0) {
            s_logger.info("Size of device readings is {}", deviceDataToSend.getDeviceReadings().size());
        } else {
            s_logger.info("The device does not contain any device readings :(");
        }

        for (DeviceReading deviceReading : deviceDataToSend.getDeviceReadings()) {
            CommnStructuresProtos.ReadingType readingType = null;
            if (deviceReading.getReadingType() == ReadingType.SENSOR_CURRENT_VALUE)
                readingType = CommnStructuresProtos.ReadingType.SENSOR_CURRENT_VALUE;
            else if (deviceReading.getReadingType() == ReadingType.SENSOR_OTHER_VALUE)
                readingType = CommnStructuresProtos.ReadingType.SENSOR_OTHER_VALUE;
            String value = deviceReading.getValue();
            CommnStructuresProtos.DeviceReadings.Builder readings = CommnStructuresProtos.DeviceReadings.newBuilder();
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