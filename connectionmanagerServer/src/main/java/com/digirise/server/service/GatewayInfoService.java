package com.digirise.server.service;

import com.digirise.server.controller.GatewayResponseDTO;
import com.digirise.server.model.Gateway;
import com.digirise.server.model.GatewayRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-06
 * Author: shrinkhlak
 */

@Service
public class GatewayInfoService {
    public static final Logger s_logger = LoggerFactory.getLogger(GatewayInfoService.class);
    @Autowired
    public GatewayRepository gatewayRepository;

    public GatewayResponseDTO getGatewayInfoById(long gatewayId){
        Optional<Gateway> gateway = gatewayRepository.findById(gatewayId);
        if (gateway.isPresent()) {
            Gateway gateway1 = gateway.get();
            GatewayResponseDTO gatewayResponseDTO = new GatewayResponseDTO();
            gatewayResponseDTO.setCustomerId(gateway1.getCustomer().getCustomerId());
            gatewayResponseDTO.setGatewayId(gateway1.getGatewayId());
            gatewayResponseDTO.setName(gateway1.getName());
            gatewayResponseDTO.setCoordinates(gateway1.getCoordinates());
            gatewayResponseDTO.setLocation(gateway1.getLocation());
            return gatewayResponseDTO;
        } else {
            return null;
        }
    }


}
