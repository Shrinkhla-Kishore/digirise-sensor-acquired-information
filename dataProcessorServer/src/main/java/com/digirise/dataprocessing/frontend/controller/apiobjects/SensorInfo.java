package com.digirise.dataprocessing.frontend.controller.apiobjects;

import com.digirise.sai.commons.helper.DeviceType;

/**
 * SensorInfo is Data Processor server's internal object that is the same as
 * the gRPC object sensors_info.proto class --> SensorInfo
 *
 * Created by IntelliJ IDEA.
 * Date: 2021-01-19
 * Author: shrinkhlak
 */
public class SensorInfo {
    private long sensorId;
    private String name;
    private String location;
    private DeviceType deviceType;
    private String createdOn;

    public long getSensorId() {
        return sensorId;
    }

    public void setSensorId(long sensorId) {
        this.sensorId = sensorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }
}
