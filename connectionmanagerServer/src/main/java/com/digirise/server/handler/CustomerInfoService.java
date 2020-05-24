package com.digirise.server.handler;

import com.digirise.server.controller.CustomerDTO;
import com.digirise.server.controller.CustomerResponseDTO;
import com.digirise.server.controller.GatewayResponseDTO;
import com.digirise.server.model.Customer;
import com.digirise.server.model.CustomerRepository;
import com.digirise.server.model.Gateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-05
 * Author: shrinkhlak
 */

@Service
public class CustomerInfoService {
    private static final Logger s_logger = LoggerFactory.getLogger(CustomerInfoService.class);
    @Autowired
    private CustomerRepository customerRepository;

    public boolean createCustomer(CustomerDTO customerDTO) {
        s_logger.info("Customer name once again is {}", customerDTO.getCustomerName());
        Customer customer = new Customer();
        customer.setName(customerDTO.getCustomerName());
        customer.setBillingAddress(customerDTO.getBillingAddress());
        customer.setLocation(customerDTO.getLocation());
        Timestamp timestamp = new Timestamp(new Date().getTime());
        customer.setStartDate(timestamp);
        s_logger.info("Creating a new customer with name {}: {}", customer.getName(), customer.getStartDate().toString());
        customerRepository.save(customer);
        return true;
    }

    @Transactional
    public List<CustomerResponseDTO> getCustomerInformationByName(String customerName) {
        Stream<Customer> customers = customerRepository.findCustomersByName(customerName);
        List<CustomerResponseDTO> customersResponseDTO = customers.map(this::createCustomerResponseDTO)
                .collect(Collectors.toList());

//        customers.forEach(customer -> {
//            CustomerResponseDTO customerResponseDTO = new CustomerResponseDTO();
//            customerResponseDTO.setCustomer(customer.getCustomer());
//            customerResponseDTO.setCustomerName(customer.getName());
//            customerResponseDTO.setLocation(customer.getLocation());
//            customerResponseDTO.setBillingAddress(customer.getBillingAddress());
//            if (customer.getGateways()!= null && customer.getGateways().size() > 0) {
//                List<Gateway>gateways = new ArrayList<>(customer.getGateways());
//                customerResponseDTO.setGateways(gateways);
//                customerResponseDTO.setGatewayCount(gateways.size());
//            }
//            customersResponseDTO.add(customerResponseDTO);
//        });
        return customersResponseDTO;
    }

    public CustomerResponseDTO getCustomerInformationById(long customerId) {
        Optional<Customer> customer = customerRepository.findById(customerId);
        if (customer.isPresent()) {
            return createCustomerResponseDTO(customer.get());
        } else
            return null;
    }

    private CustomerResponseDTO createCustomerResponseDTO(Customer customer) {
        CustomerResponseDTO customerResponseDTO = new CustomerResponseDTO();
        customerResponseDTO.setCustomerId(customer.getCustomerId());
        customerResponseDTO.setCustomerName(customer.getName());
        customerResponseDTO.setLocation(customer.getLocation());
        customerResponseDTO.setBillingAddress(customer.getBillingAddress());
        if (customer.getGateways()!= null && customer.getGateways().size() > 0) {
            List<GatewayResponseDTO>gateways = new ArrayList<>();
            for (Gateway gw : customer.getGateways()) {
                GatewayResponseDTO gatewayResponseDTO = new GatewayResponseDTO();
                gatewayResponseDTO.setGatewayId(gw.getGatewayId());
                gatewayResponseDTO.setCustomerId(gw.getCustomer().getCustomerId());
                gatewayResponseDTO.setLocation(gw.getLocation());
                gatewayResponseDTO.setCoordinates(gw.getCoordinates());
                gatewayResponseDTO.setName(gw.getName());
                gateways.add(gatewayResponseDTO);
            }
            s_logger.info("size of arrayList containing the gateway information {}", gateways.size());
            customerResponseDTO.setGateways(gateways);
            customerResponseDTO.setGatewayCount(gateways.size());
        }
        return customerResponseDTO;
    }
}
