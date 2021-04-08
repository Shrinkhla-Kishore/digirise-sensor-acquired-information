package com.digirise.server.model;

import org.hibernate.annotations.LazyCollection;
import org.springframework.context.annotation.Lazy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-04
 * Author: shrinkhlak
 */

@Entity
public class Customer implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @NotNull
    //@Column(unique = true)
    private String customerId;
    @NotNull
    //@UniqueElements
    @Column(unique = true)
    private String name;
    @NotNull
    private Date startDate;
    private String location;
    private String billingAddress;
    private Date contractExpiryDate;
    private Date updateDate;
    @OneToMany (mappedBy = "customer")
    @Lazy
    private Set<Gateway> gateways = new HashSet<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
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

    public Date getContractExpiryDate() {
        return contractExpiryDate;
    }

    public void setContractExpiryDate(Date contractExpiryDate) {
        this.contractExpiryDate = contractExpiryDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Set<Gateway> getGateways() {
        return gateways;
    }

    public void setGateways(Set<Gateway> gateways) {
        this.gateways = gateways;
    }

    public String toString() {
        StringBuilder customerAsString = new StringBuilder("Customer Id: ");
        customerAsString.append(id);
        customerAsString.append(", CustomerId: ").append(customerId);
        customerAsString.append(", Name: ").append(name);
        customerAsString.append(", Start Date: ").append(startDate.toString());
        customerAsString.append(", Update Date: ").append(updateDate.toString());
        customerAsString.append(", Location of installation: ").append(location);
        customerAsString.append(", Billing Address: ").append(billingAddress);
        customerAsString.append(", Contract Expiry Date: ").append(contractExpiryDate);
        return customerAsString.toString();
    }
}
