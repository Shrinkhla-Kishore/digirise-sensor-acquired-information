package com.digirise.sai.commons.servercommunication;

import com.digirise.sai.commons.helper.DeviceReading;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-04-23
 * Author: shrinkhlak
 */
public class DeviceDataBetweenServers {
    private long deviceId;
    private List<DeviceReading> deviceReadings;
    private Timestamp timestamp;

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
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
