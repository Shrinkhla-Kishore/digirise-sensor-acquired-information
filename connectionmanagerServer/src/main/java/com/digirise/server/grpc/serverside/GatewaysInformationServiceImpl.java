package com.digirise.server.grpc.serverside;

import com.digirise.proto.frontendmessages.GatewaysInfoProto;
import com.digirise.proto.frontendmessages.GatewaysInformationServiceGrpc;
import com.digirise.server.model.Customer;
import com.digirise.server.model.CustomerRepository;
import com.digirise.server.model.Gateway;
import com.digirise.server.model.GatewayRepository;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

/** GatewaysInformationServiceImpl is to send gateway related information requested by the DP server.
 * Created by IntelliJ IDEA.
 * Date: 2021-01-06
 * Author: shrinkhlak
 */

@Component
public class GatewaysInformationServiceImpl extends
        GatewaysInformationServiceGrpc.GatewaysInformationServiceImplBase {
    public static final Logger s_logger = LoggerFactory.getLogger(GatewaysInformationServiceImpl.class);
    @Autowired
    private GatewayRepository gatewayRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public void getGatewaysInformation(GatewaysInfoProto.GatewaysInfoRequest gatewayInfoRequest,
                                       StreamObserver<GatewaysInfoProto.GatewaysInfoResponse> gatewaysInfoResponseObserver){

        GatewaysInfoProto.GatewaysInfoResponse.Builder gatewaysInfoRespBuilder =
                GatewaysInfoProto.GatewaysInfoResponse.newBuilder();
        GatewaysInfoProto.GatewayInfo.Builder gatewayInfoBuilder = GatewaysInfoProto.GatewayInfo.newBuilder();
        Optional<Customer> customerFromDb = customerRepository.findById(gatewayInfoRequest.getCustomerId());
        if (customerFromDb.isPresent()) {
            Stream<Gateway> gatewaysStream = gatewayRepository.findAllGatewayForCustomerId(customerFromDb.get());
            gatewaysStream.forEach((gateway) -> {
                gatewayInfoBuilder.setGatewayId(gateway.getGatewayId());
                gatewayInfoBuilder.setName(gateway.getName());
                gatewayInfoBuilder.setLocation(gateway.getLocation());
                gatewayInfoBuilder.setCoordinates(gateway.getCoordinates());
                gatewayInfoBuilder.setDiscoveryRequired(gateway.isDiscoveryRequired());
                gatewayInfoBuilder.setCreatedOn(gateway.getCreatedOn().toString());
                gatewayInfoBuilder.setLastConnectedOn(gateway.getLastConnected().toString());
                gatewayInfoBuilder.setLastUpdatedOn(gateway.getLastUpdatedOn().toString());
                gatewaysInfoRespBuilder.addGatewayInfo(gatewayInfoBuilder.build());
            });
        } else {
            s_logger.warn("No gateways for the customer Id {} can be found as customer could not be found");
        }
        gatewaysInfoResponseObserver.onNext(gatewaysInfoRespBuilder.build());
        gatewaysInfoResponseObserver.onCompleted();
    }
}
