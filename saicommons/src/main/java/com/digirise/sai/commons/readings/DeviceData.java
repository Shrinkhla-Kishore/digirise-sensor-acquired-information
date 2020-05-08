package com.digirise.sai.commons.readings;

import com.digirise.proto.GatewayDataProtos;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-04-23
 * Author: shrinkhlak
 */
public class DeviceData {
    private String deviceId;
    private GatewayDataProtos.DeviceType deviceType;
    private List<DeviceReading> deviceReadings;
    private Timestamp timestamp;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public GatewayDataProtos.DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(GatewayDataProtos.DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public List<DeviceReading> getDeviceReadings() {
        return deviceReadings;
    }

    public void setDeviceReadings(List<DeviceReading> deviceReadings) {
        this.deviceReadings = deviceReadings;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
