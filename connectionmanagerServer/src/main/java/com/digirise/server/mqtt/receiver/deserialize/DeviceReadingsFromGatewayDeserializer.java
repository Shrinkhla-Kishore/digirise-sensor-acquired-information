package com.digirise.server.mqtt.receiver.deserialize;

import com.digirise.proto.CommonStructuresProto;
import com.digirise.proto.GatewayDataProto;
import com.digirise.sai.commons.helper.DeviceReading;
import com.digirise.sai.commons.helper.ReadingType;
import com.digirise.sai.commons.readings.DeviceData;
import com.digirise.sai.commons.readings.DeviceReadingsFromGateway;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * DeviceReadingsFromGatewayDeserializer class deserializes the message received on the
 * gateway data topic.
 * Created by IntelliJ IDEA.
 * Date: 2020-04-27
 * Author: shrinkhlak
 */

@Component
public class DeviceReadingsFromGatewayDeserializer {
    public DeviceReadingsFromGateway deserializeDeviceReadingsFromGateway(
            GatewayDataProto.DevicesReadingsFromGateway deviceReadingsProtobuf) {
        DeviceReadingsFromGateway deviceReadingsFromGateway = new DeviceReadingsFromGateway();
        List<DeviceData> deviceDataList = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;;
        LocalDateTime localDateTime = LocalDateTime.from(formatter.parse(deviceReadingsProtobuf.getGatewayTimestamp()));
        deviceReadingsFromGateway.setGatewayTimestamp(Timestamp.valueOf(localDateTime));
        deviceReadingsFromGateway.setGatewayName(deviceReadingsProtobuf.getGatewayName());
        deviceReadingsFromGateway.setCustomerName(deviceReadingsProtobuf.getCustomerName());

        for (GatewayDataProto.DeviceData deviceDataProtobuf : deviceReadingsProtobuf.getDeviceDataList()) {
            DeviceData deviceData = new DeviceData();
            deviceData.setDeviceName(deviceDataProtobuf.getDeviceName());
            List<DeviceReading> readingList = new ArrayList<>();
            for (CommonStructuresProto.DeviceReadings readingsProtobuf : deviceDataProtobuf.getAllReadingsFromDeviceList()) {
                DeviceReading reading = new DeviceReading();
                if (readingsProtobuf.getReadingType() == CommonStructuresProto.ReadingType.SENSOR_CURRENT_VALUE)
                    reading.setReadingType(ReadingType.SENSOR_CURRENT_VALUE);
                else if (readingsProtobuf.getReadingType() == CommonStructuresProto.ReadingType.SENSOR_OTHER_VALUE)
                    reading.setReadingType(ReadingType.SENSOR_OTHER_VALUE);
                reading.setValue(readingsProtobuf.getValue());
                readingList.add(reading);
            }
            deviceData.setDeviceReadings(readingList);
            localDateTime = LocalDateTime.from(formatter.parse(deviceDataProtobuf.getTimestamp()));
            deviceData.setTimestamp(Timestamp.valueOf(localDateTime));
            deviceDataList.add(deviceData);
        }
        deviceReadingsFromGateway.setDeviceDataList(deviceDataList);
        return deviceReadingsFromGateway;
    }
}
