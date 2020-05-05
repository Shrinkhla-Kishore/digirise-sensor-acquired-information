package com.digirise.connectionmanager.model;

import javax.persistence.*;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-04
 * Author: shrinkhlak
 */

@Entity
public class Sensor {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long sensorId;
    private String type;
    private String location;
    @ManyToOne
    @JoinColumn(name = "gatewayId", nullable = false)
    private Gateway gatewayId;

    public long getSensorId() {
        return sensorId;
    }

    public void setSensorId(long sensorId) {
        this.sensorId = sensorId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Gateway getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(Gateway gatewayId) {
        this.gatewayId = gatewayId;
    }
}
