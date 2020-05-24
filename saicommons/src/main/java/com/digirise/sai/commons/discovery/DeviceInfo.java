package com.digirise.sai.commons.discovery;

import com.digirise.sai.commons.helper.DeviceType;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-17
 * Author: shrinkhlak
 */

public class DeviceInfo {
    private String deviceName;
    private DeviceType deviceType;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }
}
