package com.digirise.server.controller;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-05
 * Author: shrinkhlak
 */
public class CustomerDTO {
    private String customerName;
    private String billingAddress;
    private String location;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
