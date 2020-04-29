package com.digirise.sai.commons.dataobjects;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-04-25
 * Author: shrinkhlak
 */
public class DeviceReadingsFromGateway {
    private Timestamp gatewayTimestamp;
    private List<DeviceData> deviceDataList;

    public Timestamp getGatewayTimestamp() {
        return gatewayTimestamp;
    }

    public void setGatewayTimestamp(Timestamp gatewayTimestamp) {
        this.gatewayTimestamp = gatewayTimestamp;
    }

    public List<DeviceData> getDeviceDataList() {
        return deviceDataList;
    }

    public void setDeviceDataList(List<DeviceData> deviceDataList) {
        this.deviceDataList = deviceDataList;
    }
}
