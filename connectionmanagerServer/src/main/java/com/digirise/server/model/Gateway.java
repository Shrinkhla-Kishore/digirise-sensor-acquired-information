package com.digirise.server.model;

import org.hibernate.validator.constraints.UniqueElements;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
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

    public String toString() {
        StringBuilder gatewayToString = new StringBuilder();
        gatewayToString.append("gatewayId: ").append(gatewayId);
        gatewayToString.append(", Gateway Id: ").append(name);
        gatewayToString.append(", Gateway coordinates: ").append(coordinates);
        gatewayToString.append(", Gateway location: ").append(location);
        gatewayToString.append(", Discovery required: ").append(discoveryRequired);
        return gatewayToString.toString();
    }
}
