package com.digirise.server.model;

import org.hibernate.validator.constraints.UniqueElements;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-04
 * Author: shrinkhlak
 */

@Table(
        uniqueConstraints=
        @UniqueConstraint(columnNames={"name", "customerId"})
)

@Entity
public class Gateway {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long gatewayId;
    @ManyToOne
    @JoinColumn(name = "customerId", nullable = false)
    //@Column(name = "customer_id")
    private Customer customer;
    @NotNull
    private String name;
    private String coordinates;
    private String location;
    @OneToMany (mappedBy = "gateway")
    private Set<Sensor> sensors;
    private boolean discoveryRequired;
    private Date createdOn;
    private Date lastUpdatedOn;
    private Date lastConnected;

    public long getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(long gatewayId) {
        this.gatewayId = gatewayId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
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

    public boolean isDiscoveryRequired() {
        return discoveryRequired;
    }

    public void setDiscoveryRequired(boolean discoveryRequired) {
        this.discoveryRequired = discoveryRequired;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getLastUpdatedOn() {
        return lastUpdatedOn;
    }

    public void setLastUpdatedOn(Date lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
    }

    public Date getLastConnected() {
        return lastConnected;
    }

    public void setLastConnected(Date lastConnected) {
        this.lastConnected = lastConnected;
    }

    public String toString() {
        StringBuilder gatewayToString = new StringBuilder();
        gatewayToString.append("gatewayId: ").append(gatewayId);
        gatewayToString.append(", \n Gateway name: ").append(name);
        gatewayToString.append(", \n Gateway coordinates: ").append(coordinates);
        gatewayToString.append(", \n Gateway location: ").append(location);
        gatewayToString.append(", \n Discovery required: ").append(discoveryRequired);
        gatewayToString.append(", \n Created on: ").append(createdOn);
        gatewayToString.append(", \n last updated on: ").append(lastUpdatedOn);
        gatewayToString.append(", \n last Connected on: ").append(lastConnected);
        return gatewayToString.toString();
    }
}
