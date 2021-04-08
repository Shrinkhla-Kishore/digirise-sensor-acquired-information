package com.digirise.server.controller;

import java.util.Optional;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-05
 * Author: shrinkhlak
 */
public class CustomerDTO {
    private String customerId;
    private Optional<String> customerName;
    private Optional<String> billingAddress;
    private Optional<String> location;

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        if (customerName != null && customerName.isPresent())
            return customerName.get();
        else
            return null;
    }

    public String getBillingAddress() {
        if (billingAddress != null && billingAddress.isPresent())
            return billingAddress.get();
        else
            return null;
    }

    public String getLocation() {
        if (location != null && location.isPresent())
            return location.get();
        else
            return null;
    }
}
