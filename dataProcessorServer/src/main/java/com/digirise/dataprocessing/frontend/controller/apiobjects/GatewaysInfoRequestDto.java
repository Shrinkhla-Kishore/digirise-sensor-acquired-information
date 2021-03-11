package com.digirise.dataprocessing.frontend.controller.apiobjects;

/**
 * GatewaysInfoRequestDto is the request object to get the gateway info on the url /gatewayInfo
 * Created by IntelliJ IDEA.
 * Date: 2021-01-12
 * Author: shrinkhlak
 */
public class GatewaysInfoRequestDto {
    private long customerId;

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }
}
