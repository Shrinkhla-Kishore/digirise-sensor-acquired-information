package com.digirise.sai.commons.readings;

import com.digirise.sai.commons.helper.DeviceReading;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-04-23
 * Author: shrinkhlak
 */
public class DeviceData {
    private String deviceName;
    //private GatewayDataProtos.DeviceType deviceType;
    private List<DeviceReading> deviceReadings;
    private Timestamp timestamp;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

//    public GatewayDataProtos.DeviceType getDeviceType() {
//        return deviceType;
//    }
//
//    public void setDeviceType(GatewayDataProtos.DeviceType deviceType) {
//        this.deviceType = deviceType;
//    }

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
