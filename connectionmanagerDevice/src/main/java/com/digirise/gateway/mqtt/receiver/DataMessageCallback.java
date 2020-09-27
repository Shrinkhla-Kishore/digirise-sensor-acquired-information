package com.digirise.gateway.mqtt.receiver;

import com.digirise.gateway.ApplicationContextHandler;
import com.digirise.gateway.mqtt.receiver.deserialization.DeviceReadingsResponseDeserializer;
import com.digirise.proto.CommnStructuresProtos;
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

public class DataMessageCallback implements IMqttMessageListener {
    private static final Logger s_logger = LoggerFactory.getLogger(DataMessageCallback.class);
    private DeviceReadingsResponseDeserializer deviceReadingsResponseDeserializer;
    private UUID uuid;
    private AtomicBoolean responseReceived = new AtomicBoolean(false);

    public DataMessageCallback(DeviceReadingsResponseDeserializer deviceReadingsResponseDeserializer) {
        this.deviceReadingsResponseDeserializer = deviceReadingsResponseDeserializer;
    }

    public void connectionLost(Throwable throwable) {
        s_logger.info("Connection lost with the MQTT broker...try reconnecting");
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        s_logger.info("Received response to data message with UUID {} of size {} with id {} from {}", uuid, mqttMessage.getPayload().length, mqttMessage.getId(), s);

        DeviceReadingsResponseDeserializer deviceReadingsResponseDeserializer1 = ApplicationContextHandler.getBean(DeviceReadingsResponseDeserializer.class);
        s_logger.debug("deviceReadingsResponseDeserializer1 is {}", deviceReadingsResponseDeserializer1);
        ByteArrayInputStream bis = new ByteArrayInputStream(mqttMessage.getPayload());
        ObjectInput in = new ObjectInputStream(bis);
        CommnStructuresProtos.DeviceReadingsResponse responseProto = (CommnStructuresProtos.DeviceReadingsResponse) in.readObject();
        DeviceReadingsResponse response = deviceReadingsResponseDeserializer1.deserializeResponseBetweenServers(responseProto);
        s_logger.info("response is {} and  received is {}", response.toString(), response.getResponseStatus().toString());
        PublisherMessageResponsesHandler publisherCallbackFactory = ApplicationContextHandler.getBean(PublisherMessageResponsesHandler.class);
        publisherCallbackFactory.responseReceived(uuid);
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}