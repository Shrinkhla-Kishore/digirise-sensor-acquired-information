package com.digirise.dataprocessing.frontend.controller.apiobjects;

/**
 * CustomerInfoRequestDto is the request object to get the customer info on the url /customerInfo
 *
 * Created by IntelliJ IDEA.
 * Date: 2021-01-12
 * Author: shrinkhlak
 */
public class CustomerInfoRequestDto {
    private boolean allCustomers;
    private String customerId;

    public boolean isAllCustomers() {
        return allCustomers;
    }

    public void setAllCustomers(boolean allCustomers) {
        this.allCustomers = allCustomers;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
