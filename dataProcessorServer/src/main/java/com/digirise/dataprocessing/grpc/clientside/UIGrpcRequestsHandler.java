package com.digirise.dataprocessing.grpc.clientside;

import com.digirise.dataprocessing.frontend.controller.apiobjects.*;
import com.digirise.proto.CommonStructuresProto;
import com.digirise.proto.frontendmessages.*;
import com.digirise.sai.commons.helper.DeviceType;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * UIGrpcRequestsHandler class is the service layer that sends the request to the backend server and
 * creates and converts from DTO objects to internal objects.
 * It also sets up the gRPC client towards the backend server.
 * Created by IntelliJ IDEA.
 * Date: 2021-01-13
 * Author: shrinkhlak
 */

@Service
public class UIGrpcRequestsHandler {
    private static final Logger s_logger = LoggerFactory.getLogger(UIGrpcRequestsHandler.class);
    private ManagedChannel managedChannel;
//    @Autowired
//    private UIGrpcRequestsHandler uiRequestsHandler;
    @Value("${backendserver.grpc.port}")
    private String backendServerPort;
    @Value("${backendserver.grpc.host}")
    private String backendServerHost;
    private ExecutorService grpcExecutorService;

    public UIGrpcRequestsHandler() {
        grpcExecutorService = Executors.newSingleThreadExecutor();
    }

    @PostConstruct
    public void setupGrpcClient() throws UnknownHostException {
        InetAddress address = InetAddress.getByName(backendServerHost);
        s_logger.info("backendServerHost: {}, address: {}, backendServerPort: {}", backendServerHost, address, backendServerPort);
        managedChannel = ManagedChannelBuilder.forAddress(backendServerHost, Integer.parseInt(backendServerPort))
                .usePlaintext().build();
        ConnectivityState connectivityState = managedChannel.getState(true);
        try {
            while (connectivityState == ConnectivityState.CONNECTING) {
                Thread.currentThread().sleep(2000);
                connectivityState = managedChannel.getState(true);
            }
            if (connectivityState == ConnectivityState.READY || connectivityState == ConnectivityState.IDLE)
                s_logger.info("gRPC client managed channel started successfully. Connectivity State {}, Host {}, " +
                        "backendServerPort {}:)", connectivityState.name(), address.getAddress(), backendServerPort);
            else
                s_logger.error("Error connecting to grpc on backend server. Connectivity state {}", connectivityState.name());
        } catch (InterruptedException e) {
            s_logger.warn("gRPC client setup interrupted");
            managedChannel.shutdown();
            connectivityState = managedChannel.getState(true);
            if (connectivityState != ConnectivityState.SHUTDOWN) {
                managedChannel.shutdownNow();
            }
        }
    }

    public CustomerInfoResponseDto retrieveCustomerInfo(boolean allCustomers, String customerId) {
        CustomerInfoResponseDto customerInfoResponseDto = new CustomerInfoResponseDto();

        CustomersInformationServiceGrpc.CustomersInformationServiceBlockingStub blockingStub =
                CustomersInformationServiceGrpc.newBlockingStub(managedChannel);
        CustomersInformationServiceGrpc.CustomersInformationServiceStub stub =
                CustomersInformationServiceGrpc.newStub(managedChannel);
        CustomersInformationServiceGrpc.CustomersInformationServiceFutureStub futureStub =
                CustomersInformationServiceGrpc.newFutureStub(managedChannel);
        CustomersInfoProto.CustomersInfoRequest.Builder customersInfoRequestBuilder = CustomersInfoProto.CustomersInfoRequest.newBuilder();
        customersInfoRequestBuilder.setAllCustomers(allCustomers);
        customersInfoRequestBuilder.setCustomerId(customerId);
        CustomersInfoProto.CustomersInfoRequest customersInfoRequest = customersInfoRequestBuilder.build();
        s_logger.info("Sending request to ConnectionManagerServer with values {}, {}",
                customersInfoRequest.getAllCustomers(), customersInfoRequest.getCustomerId());

        CustomersInfoProto.CustomersInfoResponse customersInfoResponse = blockingStub.getCustomersInformation(customersInfoRequest);
        List<CustomerInfo> customerInfoList = new ArrayList<>();
        for (CustomersInfoProto.CustomerInfo customersInfoProto : customersInfoResponse.getCustomerInfoList()) {
            CustomerInfo customerInfo = new CustomerInfo();
            s_logger.trace("Building customer info for customer with id {}, and name {}",
                    customersInfoProto.getCustomerId(), customersInfoProto.getName());
            customerInfo.setCustomerId(customersInfoProto.getCustomerId());
            customerInfo.setName(customersInfoProto.getName());
            customerInfo.setBillingAddress(customersInfoProto.getBillingAddress());
            customerInfo.setLocation(customersInfoProto.getLocation());
            customerInfo.setStartDate(customersInfoProto.getStartDate());
            customerInfo.setContractExpiryDate(customersInfoProto.getContractExpiryDate());
            customerInfo.setTotalNumberOfGateways(customersInfoProto.getTotalNumberOfGateways());
            customerInfo.setTotalNumberOfSensors(customersInfoProto.getTotalNumberOfSensors());
            customerInfoList.add(customerInfo);
        }
        customerInfoResponseDto.setCustomerInfoList(customerInfoList);

        // USING ASYNC STUB
//        StreamObserver<CustomersInfoProto.CustomersInfoResponse> responseObserver = new StreamObserver<CustomersInfoProto.CustomersInfoResponse>() {
//            @Override
//            public void onNext(CustomersInfoProto.CustomersInfoResponse customersInfoResponse) {
//                s_logger.trace("Response received for customerInfo");
//                List<CustomerInfo> customerInfoList = new ArrayList<>();
//                for (CustomersInfoProto.CustomerInfo customersInfoProto : customersInfoResponse.getCustomerInfoList()) {
//                    CustomerInfo customerInfo = new CustomerInfo();
//                    s_logger.trace("Building customer info for customer with id {}, and name {}",
//                            customersInfoProto.getCustomerId(), customersInfoProto.getName());
//                    customerInfo.setCustomerId(customersInfoProto.getCustomerId());
//                    customerInfo.setName(customersInfoProto.getName());
//                    customerInfo.setBillingAddress(customersInfoProto.getBillingAddress());
//                    customerInfo.setLocation(customersInfoProto.getLocation());
//                    customerInfo.setStartDate(customersInfoProto.getStartDate());
//                    customerInfo.setContractExpiryDate(customersInfoProto.getContractExpiryDate());
//                    customerInfo.setTotalNumberOfGateways(customersInfoProto.getTotalNumberOfGateways());
//                    customerInfo.setTotalNumberOfSensors(customersInfoProto.getTotalNumberOfSensors());
//                    customerInfoList.add(customerInfo);
//                }
//                customerInfoResponseDto.setCustomerInfoList(customerInfoList);
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//                s_logger.warn("Error received from backend server. {}", Status.fromThrowable(throwable));
//            }
//
//            @Override
//            public void onCompleted() {
//                s_logger.info("Finished receiving the response. To send back the result to the client");
//            }
//        };
//        stub.getCustomersInformation(customersInfoRequest, responseObserver);

        //Using FUTURE STUB
//        ListenableFuture<CustomersInfoProto.CustomersInfoResponse> customersInfoResponse = futureStub.getCustomersInformation(customersInfoRequest);
//        s_logger.info("request getCustomersInformation sent to ConnectionManagerServer");
//        Futures.addCallback(customersInfoResponse, new FutureCallback<CustomersInfoProto.CustomersInfoResponse>() {
//            @Override
//            public void onSuccess(@NullableDecl CustomersInfoProto.CustomersInfoResponse customersInfoResponse) {
//                s_logger.trace("Response received for customerInfo");
//                List<CustomerInfo> customerInfoList = new ArrayList<>();
//                for (CustomersInfoProto.CustomerInfo customersInfoProto : customersInfoResponse.getCustomerInfoList()) {
//                    CustomerInfo customerInfo = new CustomerInfo();
//                    s_logger.trace("Building customer info for customer with id {}, and name {}",
//                            customersInfoProto.getCustomerId(), customersInfoProto.getName());
//                    customerInfo.setCustomerId(customersInfoProto.getCustomerId());
//                    customerInfo.setName(customersInfoProto.getName());
//                    customerInfo.setBillingAddress(customersInfoProto.getBillingAddress());
//                    customerInfo.setLocation(customersInfoProto.getLocation());
//                    customerInfo.setStartDate(customersInfoProto.getStartDate());
//                    customerInfo.setContractExpiryDate(customersInfoProto.getContractExpiryDate());
//                    customerInfo.setTotalNumberOfGateways(customersInfoProto.getTotalNumberOfGateways());
//                    customerInfo.setTotalNumberOfSensors(customersInfoProto.getTotalNumberOfSensors());
//                    customerInfoList.add(customerInfo);
//                }
//                customerInfoResponseDto.setCustomerInfoList(customerInfoList);
//            }
//
//            @Override
//            public void onFailure(Throwable throwable) {
//                s_logger.trace("Failed to receive a response for customerInfo");
//            }
//        }, grpcExecutorService);

        s_logger.trace("Returning customerInformation with {} customer info", customerInfoResponseDto.getCustomerInfoList().size());
        return customerInfoResponseDto;
    }

    public GatewaysInfoResponseDto retrieveGatewayInformation(long customerId) {
        GatewaysInformationServiceGrpc.GatewaysInformationServiceBlockingStub stub =
                GatewaysInformationServiceGrpc.newBlockingStub(managedChannel);
        GatewaysInfoProto.GatewaysInfoRequest.Builder gatewaysInfoReqBuilder =
                GatewaysInfoProto.GatewaysInfoRequest.newBuilder();
        gatewaysInfoReqBuilder.setCustomerId(customerId);
        GatewaysInfoProto.GatewaysInfoResponse response =
                stub.getGatewaysInformation(gatewaysInfoReqBuilder.build());
        GatewaysInfoResponseDto gatewaysInfoResponseDto = new GatewaysInfoResponseDto();
        List<GatewayInfo> gatewayInfoList = new ArrayList<>();
        for (GatewaysInfoProto.GatewayInfo gatewayInfoProto : response.getGatewayInfoList()){
            GatewayInfo gatewayInfo = new GatewayInfo();
            gatewayInfo.setGatewayId(gatewayInfoProto.getGatewayId());
            gatewayInfo.setName(gatewayInfoProto.getName());
            gatewayInfo.setLocation(gatewayInfoProto.getLocation());
            gatewayInfo.setCoordinates(gatewayInfoProto.getCoordinates());
            gatewayInfo.setLastConnectedOn(gatewayInfoProto.getLastConnectedOn());
            gatewayInfo.setCreatedOn(gatewayInfoProto.getCreatedOn());
            gatewayInfo.setDiscoveryRequired(gatewayInfoProto.getDiscoveryRequired());
            gatewayInfo.setTotalNumberOfSensors(gatewayInfoProto.getTotalNumberOfSensors());
            gatewayInfoList.add(gatewayInfo);
        }
        gatewaysInfoResponseDto.setGatewayInfoList(gatewayInfoList);
        return gatewaysInfoResponseDto;
    }

    public SensorsInfoResponseDto retrieveSensorsInformation(long gatewayId) {
        SensorsInformationServiceGrpc.SensorsInformationServiceBlockingStub stub =
                SensorsInformationServiceGrpc.newBlockingStub(managedChannel);
        SensorsInfoProto.SensorsInfoRequest.Builder sensorsInfoRequestBuilder =
                SensorsInfoProto.SensorsInfoRequest.newBuilder();
        sensorsInfoRequestBuilder.setGatewayId(gatewayId);
        SensorsInfoProto.SensorsInfoResponse response =
                stub.getSensorsInformation(sensorsInfoRequestBuilder.build());

        SensorsInfoResponseDto sensorsInfoResponseDto = new SensorsInfoResponseDto();
        List<SensorInfo> sensorInfoList = new ArrayList<>();
        for (SensorsInfoProto.SensorInfo sensorInfoProto : response.getSensorInfoList()) {
            SensorInfo sensorInfo = new SensorInfo();
            sensorInfo.setSensorId(sensorInfoProto.getSensorId());
            sensorInfo.setName(sensorInfoProto.getName());
            DeviceType deviceType = null;
            if (sensorInfoProto.getDeviceType() == CommonStructuresProto.DeviceType.MOTION_SENSOR)
                deviceType = DeviceType.MOTION_SENSOR;
            else if (sensorInfoProto.getDeviceType() == CommonStructuresProto.DeviceType.TEMPERATURE_SENSOR)
                deviceType = DeviceType.TEMPERATURE_SENSOR;
            else if (sensorInfoProto.getDeviceType() == CommonStructuresProto.DeviceType.HUMIDITY_SENSOR)
                deviceType = DeviceType.HUMIDITY_SENSOR;
            else if (sensorInfoProto.getDeviceType() == CommonStructuresProto.DeviceType.LIGHT_SENSOR)
                deviceType = DeviceType.LIGHT_SENSOR;
            sensorInfo.setDeviceType(deviceType);
            sensorInfo.setCreatedOn(sensorInfoProto.getCreatedOn());
            sensorInfoList.add(sensorInfo);
        }
        sensorsInfoResponseDto.setSensorInfoList(sensorInfoList);
        return sensorsInfoResponseDto;
    }
}




