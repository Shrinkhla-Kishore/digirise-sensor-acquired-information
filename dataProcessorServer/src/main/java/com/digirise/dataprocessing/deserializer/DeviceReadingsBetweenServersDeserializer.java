package com.digirise.dataprocessing.deserializer;

import com.digirise.proto.CommnStructuresProtos;
import com.digirise.proto.GatewayDataForDpProtos;
import com.digirise.sai.commons.helper.DeviceReading;
import com.digirise.sai.commons.helper.ReadingType;
import com.digirise.sai.commons.servercommunication.DeviceDataBetweenServers;
import com.digirise.sai.commons.servercommunication.DeviceReadingsBetweenServers;
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
 * Date: 2020-04-27
 * Author: shrinkhlak
 */

@Component
public class DeviceReadingsBetweenServersDeserializer {
    private static final Logger s_logger = LoggerFactory.getLogger(DeviceReadingsBetweenServersDeserializer.class);

    public DeviceReadingsBetweenServersDeserializer() {
        s_logger.info("DeviceReadingsBetweenServersDeserializer created");
    }
    public DeviceReadingsBetweenServers deserializeDeviceReadings(GatewayDataForDpProtos.DevicesReadingsBetweenServers deviceReadingsProtobuf) {
        s_logger.info("Deserializing the gRPC proto message");
        DeviceReadingsBetweenServers deviceReadingsBetweenServers = new DeviceReadingsBetweenServers();
        List<DeviceDataBetweenServers> deviceDataBetweenServersList = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;;
        LocalDateTime localDateTime = LocalDateTime.from(formatter.parse(deviceReadingsProtobuf.getGatewayTimestamp()));
        deviceReadingsBetweenServers.setGatewayTimestamp(Timestamp.valueOf(localDateTime));

        for (GatewayDataForDpProtos.DeviceDataBetweenServers deviceDataProtobuf : deviceReadingsProtobuf.getDeviceDataBetweenServersList()) {
            DeviceDataBetweenServers deviceDataBetweenServers = new DeviceDataBetweenServers();
            List<DeviceReading> deviceReadingList = new ArrayList<>();
            for (CommnStructuresProtos.DeviceReadings readingProtobuf : deviceDataProtobuf.getAllReadingsFromDeviceList()) {
                DeviceReading reading = new DeviceReading();
                if (readingProtobuf.getReadingType() == CommnStructuresProtos.ReadingType.SENSOR_CURRENT_VALUE)
                    reading.setReadingType(ReadingType.SENSOR_CURRENT_VALUE);
                else if (readingProtobuf.getReadingType() == CommnStructuresProtos.ReadingType.SENSOR_OTHER_VALUE)
                    reading.setReadingType(ReadingType.SENSOR_OTHER_VALUE);
                reading.setValue(readingProtobuf.getValue());
                reading.setUnit(readingProtobuf.getUnit());
                deviceReadingList.add(reading);
            }
            deviceDataBetweenServers.setDeviceReadings(deviceReadingList);
            deviceDataBetweenServers.setDeviceId(deviceDataProtobuf.getDeviceId());
            localDateTime = LocalDateTime.from(formatter.parse(deviceDataProtobuf.getTimestamp()));
            deviceDataBetweenServers.setTimestamp(Timestamp.valueOf(localDateTime));
            deviceDataBetweenServersList.add(deviceDataBetweenServers);
        }
        deviceReadingsBetweenServers.setDeviceDataList(deviceDataBetweenServersList);
        return deviceReadingsBetweenServers;
    }
}