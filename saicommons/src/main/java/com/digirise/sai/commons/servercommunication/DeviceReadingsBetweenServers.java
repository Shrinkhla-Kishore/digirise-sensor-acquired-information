package com.digirise.sai.commons.servercommunication;

import com.digirise.sai.commons.readings.DeviceData;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-04-25
 * Author: shrinkhlak
 */
public class DeviceReadingsBetweenServers {
    private Timestamp gatewayTimestamp;
    private List<DeviceDataBetweenServers> deviceDataList;

    public Timestamp getGatewayTimestamp() {
        return gatewayTimestamp;
    }

    public void setGatewayTimestamp(Timestamp gatewayTimestamp) {
        this.gatewayTimestamp = gatewayTimestamp;
    }

    public List<DeviceDataBetweenServers> getDeviceDataList() {
        return deviceDataList;
    }

    public void setDeviceDataList(List<DeviceDataBetweenServers> deviceDataList) {
        this.deviceDataList = deviceDataList;
    }
}
