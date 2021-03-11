package com.digirise.gateway.mqtt.receiver;

import com.digirise.gateway.ApplicationContextHandler;
import com.digirise.proto.CommonStructuresProto;
import com.digirise.sai.commons.deserializer.DeviceReadingsResponseDeserializer;
import com.digirise.sai.commons.helper.DeviceReadingsResponse;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/** GatewayDiscoveryMessageCallback is used to handle the response to the Gateway discovery message.
 * On receiving a response it also notifies the PublisherMessageResponsesHandler bean.
 * Created by IntelliJ IDEA.
 * Author: shrinkhlak
 */

public class GatewayDiscoveryMessageCallback implements IMqttMessageListener {
    private static final Logger s_logger = LoggerFactory.getLogger(GatewayDiscoveryMessageCallback.class);
    // TODO: Remove the DeviceReadingsResponseDeserializer varaible and the constructor setting this variable's value
    private DeviceReadingsResponseDeserializer deviceReadingsResponseDeserializer;
    private UUID uuid;
    private AtomicBoolean responseReceived = new AtomicBoolean(false);

    public GatewayDiscoveryMessageCallback(DeviceReadingsResponseDeserializer deviceReadingsResponseDeserializer) {
        this.deviceReadingsResponseDeserializer = deviceReadingsResponseDeserializer;
    }

    public void connectionLost(Throwable throwable) {
        s_logger.info("Connection lost with the MQTT broker...try reconnecting");
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        s_logger.info("Received response for discovery message of size {} with id {} from {}", mqttMessage.getPayload().length, mqttMessage.getId(), s);

        DeviceReadingsResponseDeserializer deviceReadingsResponseDeserializer1 = ApplicationContextHandler.getBean(DeviceReadingsResponseDeserializer.class);
        s_logger.debug("deviceReadingsResponseDeserializer1 is {}", deviceReadingsResponseDeserializer1);
        ByteArrayInputStream bis = new ByteArrayInputStream(mqttMessage.getPayload());
        ObjectInput in = new ObjectInputStream(bis);
        CommonStructuresProto.DeviceReadingsResponse responseProto = (CommonStructuresProto.DeviceReadingsResponse) in.readObject();
        DeviceReadingsResponse response = deviceReadingsResponseDeserializer1.deserializeResponseBetweenServers(responseProto);
        s_logger.info("response is {} and  received is {}", response.toString(), response.getResponseStatus().toString());
        PublisherMessageResponsesHandler publisherMessageResponsesHandler = ApplicationContextHandler.getBean(PublisherMessageResponsesHandler.class);
        publisherMessageResponsesHandler.discoveryMessageResponseReceived();
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
