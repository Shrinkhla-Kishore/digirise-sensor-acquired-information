package com.digirise.server.mqtt.receiver.deserialization;

import com.digirise.proto.GatewayDiscoveryProtos;
import com.digirise.sai.commons.discovery.GatewayDiscovery;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-08
 * Author: shrinkhlak
 */

@Component
public class GatewayDiscoveryDeserializer {
    public GatewayDiscovery deserializeGatewayDiscovery(GatewayDiscoveryProtos.GatewayDiscovery gatewayDiscoveryProto) {
        GatewayDiscovery gatewayDiscovery = new GatewayDiscovery();
        gatewayDiscovery.setGatewayName(gatewayDiscoveryProto.getGatewayName());
        gatewayDiscovery.setCustomerName(gatewayDiscoveryProto.getCustomerName());
        gatewayDiscovery.setCustomerId(gatewayDiscoveryProto.getCustomerId());
        gatewayDiscovery.setLocation(gatewayDiscoveryProto.getLocation());
        gatewayDiscovery.setCoordinates(gatewayDiscoveryProto.getCoordinates());
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;;
        LocalDateTime localDateTime = LocalDateTime.from(formatter.parse(gatewayDiscoveryProto.getTimestamp()));
        gatewayDiscovery.setTimestamp(Timestamp.valueOf(localDateTime));
        return gatewayDiscovery;
    }
}
