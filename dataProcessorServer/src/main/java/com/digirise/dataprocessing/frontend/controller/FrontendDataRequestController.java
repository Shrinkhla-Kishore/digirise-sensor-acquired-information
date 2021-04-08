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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.async.WebAsyncTask;

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

//    @PostMapping(path = "/customerInfo")
//    public ResponseEntity<CustomerInfoResponseDto> retrieveCustomerInfo(@RequestBody CustomerInfoRequestDto customerInfoRequestDto) {
//        if (customerInfoRequestDto == null) {
//            s_logger.error("Bad request, none of the required parameter is specified");
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
//        s_logger.trace("customer info requested for {} and all is {}",
//                customerInfoRequestDto.getCustomerId(), customerInfoRequestDto.isAllCustomers());
//        boolean isAllCustomers = customerInfoRequestDto.isAllCustomers();
//        String customerId = customerInfoRequestDto.getCustomerId();
//        if (isAllCustomers == false && (customerId == null || customerId.length() == 0)) {
//            // return error code
//            s_logger.error("Bad request, none of the required parameter is specified");
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
//        else {
//            CustomerInfoResponseDto response = uiRequestsHandler.retrieveCustomerInfo(isAllCustomers, customerId);
//            s_logger.trace("response has {} entries", response.getCustomerInfoList().size());
//            return new ResponseEntity<>(response, HttpStatus.OK);
//        }
//    }

    @PostMapping(path = "/customerInfo")
    public WebAsyncTask<CustomerInfoResponseDto> retrieveCustomerInfo(@RequestBody CustomerInfoRequestDto customerInfoRequestDto) {
        WebAsyncTask<CustomerInfoResponseDto> task = new WebAsyncTask<CustomerInfoResponseDto>(5000, () -> {
            if (customerInfoRequestDto == null) {
                s_logger.error("Bad request, request object cannot be null");
                throw new RuntimeException("Bad request: request object cannot be null");
            }
            s_logger.trace("customer info requested for {} and all is {}",
                    customerInfoRequestDto.getCustomerId(), customerInfoRequestDto.isAllCustomers());
            boolean isAllCustomers = customerInfoRequestDto.isAllCustomers();
            String customerId = customerInfoRequestDto.getCustomerId();
            if (isAllCustomers == false && (customerId == null || customerId.length() == 0)) {
                // return error code
                s_logger.error("Bad request, none of the required parameter is specified");
                throw new RuntimeException("Bad request: specify at least one parameter");
            }
            else {
                CustomerInfoResponseDto response = uiRequestsHandler.retrieveCustomerInfo(isAllCustomers, customerId);
                s_logger.trace("response has {} entries", response.getCustomerInfoList().size());
                return response;
            }
        });
        s_logger.trace("Returning from method, to verify Async behavior :)");
        return task;
    }

//    @PostMapping(path = "/gatewayInfo")
//    public ResponseEntity<GatewaysInfoResponseDto> retreiveGatewayInfo(@RequestBody GatewaysInfoRequestDto gatewaysInfoRequestDto){
//        long customerId = gatewaysInfoRequestDto.getCustomerId();
//        if (customerId > 0){
//            GatewaysInfoResponseDto response = uiRequestsHandler.retrieveGatewayInformation(customerId);
//            return new ResponseEntity<>(response, HttpStatus.OK);
//        } else
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//    }

    @PostMapping(path = "/gatewayInfo")
    public WebAsyncTask<GatewaysInfoResponseDto> retreiveGatewayInfo(@RequestBody GatewaysInfoRequestDto gatewaysInfoRequestDto){
        long customerId = gatewaysInfoRequestDto.getCustomerId();
        WebAsyncTask<GatewaysInfoResponseDto> task = new WebAsyncTask<GatewaysInfoResponseDto>(5000, () -> {
            if (customerId > 0){
                GatewaysInfoResponseDto response = uiRequestsHandler.retrieveGatewayInformation(customerId);
                return response;
            } else
                throw new RuntimeException("BAD REQUEST: customer Id should be provided");
        });
        return task;
    }

    @PostMapping(path = "/sensorInfo")
    public WebAsyncTask<SensorsInfoResponseDto> retrieveSensorsInfo(@RequestBody SensorsInfoRequestDto sensorsInfoRequestDto) {
        WebAsyncTask<SensorsInfoResponseDto> task = new WebAsyncTask<SensorsInfoResponseDto>(5000, () -> {
            long gatewayId = sensorsInfoRequestDto.getGatewayId();
            if (gatewayId > 0) {
                SensorsInfoResponseDto response = uiRequestsHandler.retrieveSensorsInformation(gatewayId);
                return response;
            } else
                throw new RuntimeException("BAD REQUEST: Gateway Id must be specified");
        });
        return task;
    }

}
