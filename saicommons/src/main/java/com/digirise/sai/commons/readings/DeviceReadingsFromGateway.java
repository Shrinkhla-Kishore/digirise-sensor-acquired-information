package com.digirise.sai.commons.readings;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-04-25
 * Author: shrinkhlak
 */
public class DeviceReadingsFromGateway {
    private Timestamp gatewayTimestamp;
    private String gatewayName;
    private String customerName;
    private List<DeviceData> deviceDataList;

    public Timestamp getGatewayTimestamp() {
        return gatewayTimestamp;
    }

    public void setGatewayTimestamp(Timestamp gatewayTimestamp) {
        this.gatewayTimestamp = gatewayTimestamp;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<DeviceData> getDeviceDataList() {
        return deviceDataList;
    }

    public void setDeviceDataList(List<DeviceData> deviceDataList) {
        this.deviceDataList = deviceDataList;
    }
}
