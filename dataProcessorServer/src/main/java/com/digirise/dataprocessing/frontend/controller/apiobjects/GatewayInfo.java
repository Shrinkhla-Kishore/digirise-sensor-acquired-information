package com.digirise.dataprocessing.frontend.controller.apiobjects;

/**
 * GatewayInfo is Data Processor server's internal object that is the same as
 * the gRPC object gateways_info.proto class --> GatewayInfo
 * Created by IntelliJ IDEA.
 * Date: 2021-01-19
 * Author: shrinkhlak
 */
public class GatewayInfo {
    private long gatewayId;
    private String name;
    private String location;
    private String coordinates;
    private boolean discoveryRequired;
    private String createdOn;
    private String lastConnectedOn;

    public long getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(long gatewayId) {
        this.gatewayId = gatewayId;
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

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public boolean isDiscoveryRequired() {
        return discoveryRequired;
    }

    public void setDiscoveryRequired(boolean discoveryRequired) {
        this.discoveryRequired = discoveryRequired;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getLastConnectedOn() {
        return lastConnectedOn;
    }

    public void setLastConnectedOn(String lastConnectedOn) {
        this.lastConnectedOn = lastConnectedOn;
    }
}
