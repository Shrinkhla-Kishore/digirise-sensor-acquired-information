package com.digirise.dataprocessing.frontend.controller.apiobjects;

import java.util.List;

/**
 * GatewaysInfoResponseDto is the response object that contains the gateway info on the url /gatewayInfo
 * Created by IntelliJ IDEA.
 * Date: 2021-01-12
 * Author: shrinkhlak
 */
public class GatewaysInfoResponseDto {
    List<GatewayInfo> gatewayInfoList;

    public List<GatewayInfo> getGatewayInfoList() {
        return gatewayInfoList;
    }

    public void setGatewayInfoList(List<GatewayInfo> gatewayInfoList) {
        this.gatewayInfoList = gatewayInfoList;
    }
}
