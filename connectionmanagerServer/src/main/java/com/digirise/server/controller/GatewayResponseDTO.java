package com.digirise.server.controller;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-06
 * Author: shrinkhlak
 */
public class GatewayResponseDTO {
    private long gatewayId;
    private long customerId;
    private String name;
    private String coordinates;
    private String location;

    public long getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(long gatewayId) {
        this.gatewayId = gatewayId;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
