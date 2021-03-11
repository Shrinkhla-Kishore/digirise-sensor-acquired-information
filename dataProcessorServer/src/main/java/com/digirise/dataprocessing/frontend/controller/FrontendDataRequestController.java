package com.digirise.dataprocessing.frontend.controller;

import com.digirise.dataprocessing.frontend.controller.apiobjects.*;
import com.digirise.dataprocessing.grpc.clientside.UIGrpcRequestsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

/**
 * FrontendDataRequestController is a REST controller class that defines the request and response objects
 * that daat processor server uses to request information from the backend server.
 * This information is used to populate the SAI gui.
 * Created by IntelliJ IDEA.
 * Date: 2020-05-11
 * Author: shrinkhlak
 */
@RestController
@RequestMapping(path = "/frontend")
public class FrontendDataRequestController {
    private static final Logger s_logger = LoggerFactory.getLogger(FrontendDataRequestController.class);
    @Autowired
    private UIGrpcRequestsHandler uiRequestsHandler;

    @PostConstruct
    public void controllerCreated() {
        s_logger.info("Frontend data request controller created!!");
    }

    @PostMapping(path = "/customerInfo")
    public ResponseEntity<CustomerInfoResponseDto> retrieveCustomerInfo(@RequestBody CustomerInfoRequestDto customerInfoRequestDto) {
        if (customerInfoRequestDto == null) {
            s_logger.error("Bad request, none of the required parameter is specified");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        s_logger.trace("customer info requested for {} and all is {}",
                customerInfoRequestDto.getCustomerId(), customerInfoRequestDto.isAllCustomers());
        boolean isAllCustomers = customerInfoRequestDto.isAllCustomers();
        long customerId = customerInfoRequestDto.getCustomerId();
        if (isAllCustomers == false && customerId <= 0) {
            // return error code
            s_logger.error("Bad request, none of the required parameter is specified");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        else {
            CustomerInfoResponseDto response = uiRequestsHandler.retrieveCustomerInfo(isAllCustomers, customerId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    @PostMapping(path = "/gatewayInfo")
    public ResponseEntity<GatewaysInfoResponseDto> retreiveGatewayInfo(@RequestBody GatewaysInfoRequestDto gatewaysInfoRequestDto){
        long customerId = gatewaysInfoRequestDto.getCustomerId();
        if (customerId > 0){
            GatewaysInfoResponseDto response = uiRequestsHandler.retrieveGatewayInformation(customerId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping(path = "/sensorInfo")
    public ResponseEntity<SensorsInfoResponseDto> retrieveSensorsInfo(@RequestBody SensorsInfoRequestDto sensorsInfoRequestDto) {
        long gatewayId = sensorsInfoRequestDto.getGatewayId();
        if (gatewayId > 0) {
            SensorsInfoResponseDto response = uiRequestsHandler.retrieveSensorsInformation(gatewayId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
