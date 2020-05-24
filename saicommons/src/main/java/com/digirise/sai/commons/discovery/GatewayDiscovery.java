package com.digirise.sai.commons.discovery;

import com.digirise.sai.commons.helper.DeviceType;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-07
 * Author: shrinkhlak
 */
public class GatewayDiscovery {
    private String gatewayName;
    private String customerName;
    private long customerId;
    private String coordinates;
    private String location;
    private List<DeviceInfo> deviceIds;
    private Timestamp timestamp;

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

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<DeviceInfo> getDeviceIds() {
        return deviceIds;
    }

    public void setDeviceIds(List<DeviceInfo> deviceIds) {
        this.deviceIds = deviceIds;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

}


