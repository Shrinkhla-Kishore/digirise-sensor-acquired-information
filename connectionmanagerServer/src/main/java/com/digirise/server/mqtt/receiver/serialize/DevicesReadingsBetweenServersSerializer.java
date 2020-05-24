package com.digirise.server.mqtt.receiver.serialize;

import com.digirise.proto.CommnStructuresProtos;
import com.digirise.proto.GatewayDataForDpProtos;
import com.digirise.sai.commons.helper.DeviceReading;
import com.digirise.sai.commons.helper.ReadingType;
import com.digirise.sai.commons.servercommunication.DeviceDataBetweenServers;
import com.digirise.sai.commons.servercommunication.DeviceReadingsBetweenServers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-04-24
 * Author: shrinkhlak
 */

@Component
public class DevicesReadingsBetweenServersSerializer {
    private static final Logger s_logger = LoggerFactory.getLogger(DevicesReadingsBetweenServersSerializer.class);

    public GatewayDataForDpProtos.DevicesReadingsBetweenServers serialize(DeviceReadingsBetweenServers deviceReadingsBetweenServers){
        s_logger.info("Serializing gatewayReadings to be sent to Data processing application ... connected number of devices {}",
                deviceReadingsBetweenServers.getDeviceDataList().size());
        GatewayDataForDpProtos.DevicesReadingsBetweenServers.Builder devicesReadingsBetweenServersProto =
                GatewayDataForDpProtos.DevicesReadingsBetweenServers.newBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        String timestampAsString = formatter.format(deviceReadingsBetweenServers.getGatewayTimestamp().toLocalDateTime());
        devicesReadingsBetweenServersProto.setGatewayTimestamp(timestampAsString);
        for (DeviceDataBetweenServers deviceDataBetweenServers : deviceReadingsBetweenServers.getDeviceDataList()) {
            devicesReadingsBetweenServersProto.addDeviceDataBetweenServers(serializeDeviceData(deviceDataBetweenServers));
        }
        return devicesReadingsBetweenServersProto.build();
    }

    private GatewayDataForDpProtos.DeviceDataBetweenServers serializeDeviceData(DeviceDataBetweenServers deviceDataToSend) {
        s_logger.info("Serializing device data to be sent to DP ...");
        GatewayDataForDpProtos.DeviceDataBetweenServers.Builder deviceDataBuilder = GatewayDataForDpProtos.DeviceDataBetweenServers.newBuilder();
        deviceDataBuilder.setDeviceId(deviceDataToSend.getDeviceId());
        s_logger.info("building protobuf for device id {}.", deviceDataBuilder.getDeviceId());
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
            readings.setUnit(deviceReading.getUnit());
            deviceDataBuilder.addAllReadingsFromDevice(readings.build());
            s_logger.info("Readings put in device is {}, {}", readingType.toString(), value);
        }
        if (deviceDataToSend.getTimestamp() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            String timestampAsString = formatter.format(deviceDataToSend.getTimestamp().toLocalDateTime());
            deviceDataBuilder.setTimestamp(timestampAsString);
        }
        return deviceDataBuilder.build();
    }

}
