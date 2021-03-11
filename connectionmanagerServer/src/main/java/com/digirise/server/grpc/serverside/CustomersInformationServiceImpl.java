package com.digirise.server.grpc.serverside;

import com.digirise.proto.frontendmessages.CustomersInfoProto;
import com.digirise.proto.frontendmessages.CustomersInformationServiceGrpc;
import com.digirise.server.model.Customer;
import com.digirise.server.model.CustomerRepository;
import com.digirise.server.model.Gateway;
import io.grpc.stub.StreamObserver;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * CustomersInformationServiceImpl is to send customer related information requested by the DP server.
 * Created by IntelliJ IDEA.
 * Date: 2021-01-06
 * Author: shrinkhlak
 */

@Component
public class CustomersInformationServiceImpl extends
        CustomersInformationServiceGrpc.CustomersInformationServiceImplBase {
    private static final Logger s_logger = LoggerFactory.getLogger(CustomersInformationServiceImpl.class);

    @Autowired
    public CustomerRepository customerRepository;

    @Override
    public void getCustomersInformation(CustomersInfoProto.CustomersInfoRequest customersInfoRequest,
                                        StreamObserver<CustomersInfoProto.CustomersInfoResponse> customersInfoResponseObserver){
        s_logger.info("Received request to get customer information {}, {}",
                customersInfoRequest.getAllCustomers(), customersInfoRequest.getCustomerId());
        Iterable<Customer> iterables = null; // TODO: Initialize this, null is not a good option
        Stream<Customer> customerStream = null;
        if (customersInfoRequest.getAllCustomers() == true) {
            iterables = customerRepository.findAll();
            customerStream = StreamSupport.stream(iterables.spliterator(), false);
        } else {
            customerStream = customerRepository.findCustomersByIdCust(customersInfoRequest.getCustomerId());
        }
        CustomersInfoProto.CustomersInfoResponse.Builder customerInfoRespBuilder =
                CustomersInfoProto.CustomersInfoResponse.newBuilder();
        CustomersInfoProto.CustomerInfo.Builder customerInfoBuilder = CustomersInfoProto.CustomerInfo.newBuilder();

        customerStream.forEach((customer -> {
            customerInfoBuilder.setCustomerId(customer.getCustomerId());
            customerInfoBuilder.setName(customer.getName());
            customerInfoBuilder.setBillingAddress(customer.getBillingAddress());
            customerInfoBuilder.setLocation(customer.getLocation());
            customerInfoBuilder.setStartDate(customer.getStartDate().toString());
            customerInfoBuilder.setContractExpiryDate(customer.getContractExpiryDate().toString());
            customerInfoBuilder.setTotalNumberOfGateways(customer.getGateways().size());
            int totalSensors = 0;
            for (Gateway gateway : customer.getGateways()) {
                totalSensors += gateway.getSensors().size();
            }
            customerInfoBuilder.setTotalNumberOfSensors(totalSensors);
            customerInfoRespBuilder.addCustomerInfo(customerInfoBuilder.build());
        }));
        customersInfoResponseObserver.onNext(customerInfoRespBuilder.build());
        customersInfoResponseObserver.onCompleted();
    }
}
