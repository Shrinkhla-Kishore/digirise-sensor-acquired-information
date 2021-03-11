package com.digirise.dataprocessing.frontend.controller.apiobjects;

/**
 * SensorsInfoRequestDto is the request object to get the sensor info on the url /sensorInfo
 * Created by IntelliJ IDEA.
 * Date: 2021-01-12
 * Author: shrinkhlak
 */
public class SensorsInfoRequestDto {
    private long gatewayId;

    public long getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(long gatewayId) {
        this.gatewayId = gatewayId;
    }
}
