package com.digirise.server.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-04
 * Author: shrinkhlak
 */

@Entity
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long customerId;
    @NotNull
    private String name;
    @NotNull
    private Date startDate;
    private String location;
    private String billingAddress;
    @OneToMany (mappedBy = "customer")
    private Set<Gateway> gateways;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public Set<Gateway> getGateways() {
        return gateways;
    }

    public void setGateways(Set<Gateway> gateways) {
        this.gateways = gateways;
    }

    public String toString() {
        StringBuilder customerAsString = new StringBuilder("Customer Id: ");
        customerAsString.append(customerId);
        customerAsString.append(", Name: ").append(name);
        customerAsString.append(", Start Date: ").append(startDate.toString());
        customerAsString.append(", Location of installation: ").append(location);
        customerAsString.append(", Billing Address: ").append(billingAddress);
        return customerAsString.toString();
    }
}
