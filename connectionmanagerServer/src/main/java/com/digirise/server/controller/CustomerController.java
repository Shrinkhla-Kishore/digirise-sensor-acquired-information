package com.digirise.server.controller;

import com.digirise.server.handler.CustomerInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CustomerController is the ReST endpoint which could be used to create and manage
 * customer related information which gets stored in SQL database.
 * Created by IntelliJ IDEA.
 * Date: 2020-05-05
 * Author: shrinkhlak
 */

@RestController
@RequestMapping(path = "/customer")
public class CustomerController {
    public static final Logger s_logger = LoggerFactory.getLogger(CustomerController.class);
    @Autowired
    private CustomerInfoService customerInfoService;

    @PostMapping(path = "/create")
    public ResponseEntity<Void> createCustomer(@RequestBody CustomerDTO customerDTO) {
        ResponseEntity<Void> responseEntity;
        if (customerDTO != null) {
            s_logger.info("Received customer with name {}, {}, {}", customerDTO.getCustomerName(), customerDTO.getBillingAddress(), customerDTO.getLocation());
            boolean status = customerInfoService.createCustomer(customerDTO);
            if (status == true)
                responseEntity = new ResponseEntity<>(HttpStatus.OK);
            else
                responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } else
            responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        return responseEntity;
    }

    @PostMapping(path = "/update")
    public ResponseEntity<Void> updateCustomer(@RequestBody CustomerDTO customerDTO) {
        ResponseEntity<Void> responseEntity;
        if (customerDTO != null && customerDTO.getCustomerId() != null) {
            s_logger.info("Received customer with name {}, {}, {}", customerDTO.getCustomerName(), customerDTO.getBillingAddress(), customerDTO.getLocation());
            boolean status = customerInfoService.updateCustomer(customerDTO);
            if (status == true)
                responseEntity = new ResponseEntity<>(HttpStatus.OK);
            else {
                s_logger.warn("No customer found with customerId {}", customerDTO.getCustomerId());
                responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else {
            s_logger.warn("Bad request received, the request object or the customerId is null");
            responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return responseEntity;
    }

    @GetMapping(path ="/{customerId}/info")
    public ResponseEntity<CustomerResponseDTO> getCustomerInfoById(@PathVariable long customerId) {
        CustomerResponseDTO customerResponseDTO = customerInfoService.getCustomerInformationById(customerId);
        ResponseEntity<CustomerResponseDTO> response = new ResponseEntity<>(customerResponseDTO, HttpStatus.OK);
        return response;
    }

    @GetMapping(path = "/{customerName}/infos")
    public ResponseEntity<List<CustomerResponseDTO>> getCustomerInfoByName(@PathVariable String customerName){
        List<CustomerResponseDTO> customerResponseDTOs = customerInfoService.getCustomerInformationByName(customerName);
        return new ResponseEntity<List<CustomerResponseDTO>>(customerResponseDTOs, HttpStatus.OK);
    }
}
