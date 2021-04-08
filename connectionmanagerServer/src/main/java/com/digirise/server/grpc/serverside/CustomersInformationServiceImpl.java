package com.digirise.server.grpc.serverside;

import com.digirise.proto.frontendmessages.CustomersInfoProto;
import com.digirise.proto.frontendmessages.CustomersInformationServiceGrpc;
import com.digirise.server.model.Customer;
import com.digirise.server.model.CustomerRepository;
import com.digirise.server.model.Gateway;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
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
    @Transactional
    public void getCustomersInformation(CustomersInfoProto.CustomersInfoRequest customersInfoRequest,
                                        StreamObserver<CustomersInfoProto.CustomersInfoResponse> customersInfoResponseObserver){
        s_logger.info("Received request to get customer information {}, {}",
                customersInfoRequest.getAllCustomers(), customersInfoRequest.getCustomerId());
        customersInfoResponseObserver.onNext(getCustomers(customersInfoRequest));
        customersInfoResponseObserver.onCompleted();
    }

    private CustomersInfoProto.CustomersInfoResponse getCustomers(CustomersInfoProto.CustomersInfoRequest customersInfoRequest) {
        Iterable<Customer> iterables = null; // TODO: Initialize this, null is not a good option
        Stream<Customer> customerStream = null;
        if (customersInfoRequest.getAllCustomers() == true) {
            iterables = customerRepository.findAll();
            customerStream = StreamSupport.stream(iterables.spliterator(), false);
        } else {
            customerStream = customerRepository.findCustomerByCustomerId(customersInfoRequest.getCustomerId());
        }
        CustomersInfoProto.CustomersInfoResponse.Builder customerInfoRespBuilder =
                CustomersInfoProto.CustomersInfoResponse.newBuilder();
        CustomersInfoProto.CustomerInfo.Builder customerInfoBuilder = CustomersInfoProto.CustomerInfo.newBuilder();

        customerStream.forEach((customer -> {
            s_logger.trace("Building customer with id {} and name {}", customer.getCustomerId(), customer.getName());
            customerInfoBuilder.setCustomerId(customer.getCustomerId());
            customerInfoBuilder.setName(customer.getName());
            customerInfoBuilder.setBillingAddress(customer.getBillingAddress());
            customerInfoBuilder.setLocation(customer.getLocation());
            customerInfoBuilder.setStartDate(customer.getStartDate().toString());
            if (customer.getContractExpiryDate() != null)
                customerInfoBuilder.setContractExpiryDate(customer.getContractExpiryDate().toString());
            if (customer.getGateways() != null) {
                s_logger.trace("Total number of gateways found is {}", customer.getGateways().size());
                customerInfoBuilder.setTotalNumberOfGateways(customer.getGateways().size());
            }
            int totalSensors = 0;
            for (Gateway gateway : customer.getGateways()) {
                s_logger.trace("gateway {} has {} sensors", gateway.getName(), gateway.getSensors().size());
                totalSensors += gateway.getSensors().size();
            }
            customerInfoBuilder.setTotalNumberOfSensors(totalSensors);
            customerInfoRespBuilder.addCustomerInfo(customerInfoBuilder.build());
        }));
        return customerInfoRespBuilder.build();
    }
}
