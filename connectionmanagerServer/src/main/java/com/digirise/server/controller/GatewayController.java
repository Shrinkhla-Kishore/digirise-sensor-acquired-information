package com.digirise.server.controller;

import com.digirise.server.handler.GatewayInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-06
 * Author: shrinkhlak
 */

@RestController
@RequestMapping("/gateway")
public class GatewayController {
    public static final Logger s_logger = LoggerFactory.getLogger(GatewayController.class);
    @Autowired
    private GatewayInfoService gatewayInfoService;

    @PostMapping("/{gatewayId}/info")
    public ResponseEntity<GatewayResponseDTO> getGatewayInfoById(@PathVariable long gatewayId){
        GatewayResponseDTO gatewayResponseDTO = gatewayInfoService.getGatewayInfoById(gatewayId);
        if (gatewayResponseDTO != null) {
            return new ResponseEntity<GatewayResponseDTO>(gatewayResponseDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<GatewayResponseDTO>(HttpStatus.BAD_REQUEST);
        }
    }
}
