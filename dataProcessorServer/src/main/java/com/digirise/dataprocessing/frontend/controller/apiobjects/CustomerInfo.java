package com.digirise.dataprocessing.frontend.controller.apiobjects;

/**
 * CustomerInfo is Data Processor server's internal object that is the same as
 * the gRPC object customers_info.proto class --> CustomerInfo
 *
 * Created by IntelliJ IDEA.
 * Date: 2021-01-19
 * Author: shrinkhlak
 */
public class CustomerInfo {
    private long customerId;
    private String name;
    private String billingAddress;
    private String location;
    private String startDate;
    private String contractExpiryDate;
    private int totalNumberOfGateways;
    private int totalNumberOfSensors;

    public CustomerInfo() {
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getContractExpiryDate() {
        return contractExpiryDate;
    }

    public void setContractExpiryDate(String contractExpiryDate) {
        this.contractExpiryDate = contractExpiryDate;
    }

    public int getTotalNumberOfGateways() {
        return totalNumberOfGateways;
    }

    public void setTotalNumberOfGateways(int totalNumberOfGateways) {
        this.totalNumberOfGateways = totalNumberOfGateways;
    }

    public int getTotalNumberOfSensors() {
        return totalNumberOfSensors;
    }

    public void setTotalNumberOfSensors(int totalNumberOfSensors) {
        this.totalNumberOfSensors = totalNumberOfSensors;
    }
}
