package com.digirise.connectionmanager.model;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-04
 * Author: shrinkhlak
 */

@Entity
public class Gateway {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long gatewayId;
    @ManyToOne
    @JoinColumn(name = "customerId", nullable = false)
    private Customer customerId;
    private String name;
    private String coordinates;
    private String location;
    @OneToMany (mappedBy = "gatewayId")
    private Set<Sensor> sensors;


    public long getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(long gatewayId) {
        this.gatewayId = gatewayId;
    }

    public Customer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Customer customerId) {
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

    public Set<Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(Set<Sensor> sensors) {
        this.sensors = sensors;
    }

    public String toString() {
        StringBuilder gatewayToString = new StringBuilder();
        gatewayToString.append("gatewayId: ").append(gatewayId);
        gatewayToString.append(", Gateway Id: ").append(name);
        gatewayToString.append(", Gateway coordinates: ").append(coordinates);
        gatewayToString.append(", Gateway location: ").append(location);
        return gatewayToString.toString();
    }
}
