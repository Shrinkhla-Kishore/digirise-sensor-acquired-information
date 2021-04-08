package com.digirise.server.controller;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-05
 * Author: shrinkhlak
 */
public @NoArgsConstructor class CustomerResponseDTO {
    private @Getter @Setter String customerId;
    private @Getter @Setter String customerName;
    private @Getter @Setter String billingAddress;
    private @Getter @Setter String location;
    private @Getter @Setter int gatewayCount;
    private @Getter @Setter List<GatewayResponseDTO> gateways;
}
