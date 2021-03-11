package com.digirise.dataprocessing.frontend.controller.apiobjects;

import java.util.List;

/**
 * CustomerInfoResponseDto is the response object that contains the customer info on the url /customerInfo
 * Created by IntelliJ IDEA.
 * Date: 2021-01-12
 * Author: shrinkhlak
 */
public class CustomerInfoResponseDto {
    private List<CustomerInfo> customerInfoList;

    public List<CustomerInfo> getCustomerInfoList() {
        return customerInfoList;
    }

    public void setCustomerInfoList(List<CustomerInfo> customerInfoList) {
        this.customerInfoList = customerInfoList;
    }
}
