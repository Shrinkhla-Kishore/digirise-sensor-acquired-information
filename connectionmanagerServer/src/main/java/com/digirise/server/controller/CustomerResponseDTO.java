package com.digirise.server.controller;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-05
 * Author: shrinkhlak
 */
public class CustomerResponseDTO extends CustomerDTO {
    private long customerId;
    private int gatewayCount;
    private List<GatewayResponseDTO> gateways;

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public int getGatewayCount() {
        return gatewayCount;
    }

    public void setGatewayCount(int gatewayCount) {
        this.gatewayCount = gatewayCount;
    }

    public List<GatewayResponseDTO> getGateways() {
        return gateways;
    }

    public void setGateways(List<GatewayResponseDTO> gateways) {
        this.gateways = gateways;
    }
}
