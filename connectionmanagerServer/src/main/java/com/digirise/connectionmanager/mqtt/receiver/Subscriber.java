package com.digirise.connectionmanager.mqtt.receiver;

import com.digirise.api.GatewayDataProtos;
import com.digirise.connectionmanager.mqtt.receiver.deserialization.DeviceReadingsFromGatewayDeserializer;
import com.digirise.sai.commons.dataobjects.DeviceData;
import com.digirise.sai.commons.dataobjects.DeviceReading;
import com.digirise.sai.commons.dataobjects.DeviceReadingsFromGateway;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class Subscriber {
    private static final Logger s_logger = LoggerFactory.getLogger(Subscriber.class);
    @Value("${mqtt.broker}")
    private String s_mqttBroker;
    @Value("${mqtt.subscribe.topic}")
    private String subscribeTopic;
    @Autowired
    private DeviceReadingsFromGatewayDeserializer deserializer;
    private String mqttClientId;
    private MqttClient mqttClient;
    private List<String> deviceConnectedClients;

    public void configureSubscriber() {
        try {
            deviceConnectedClients = new ArrayList<>();
            mqttClientId = "serverApplication";
            mqttClient = new MqttClient(s_mqttBroker, mqttClientId);
            while (!mqttClient.isConnected()){
                try {
                    Thread.currentThread().sleep(2000);
                    mqttClient.connect();
                    s_logger.info("Mqtt subscriber started. Connected to {}", s_mqttBroker);
                    subscribeTopic();
                } catch (MqttException e) {
                    s_logger.warn("Error connecting to mqtt broker {}", s_mqttBroker);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    public void subscribeTopic() {
        try {
            if (mqttClient.isConnected()){
                s_logger.info("client is connected :)");
                mqttClient.subscribe(subscribeTopic, (topic, message) -> {

                    s_logger.info("Received message of size {} bytes on topic {}", message.getPayload().length, topic);
                    // Reading the client Id
                    int prefixLength = "gateway/".length();
                    String topicTemp = topic.substring(prefixLength);
                    int lastIndex = topicTemp.indexOf("/");
                    String gatewayId = topicTemp.substring(0, lastIndex);
                    s_logger.info("Received data from gateway mqttClientId {}, message Id: {}", gatewayId, message.getId());
                    if (!deviceConnectedClients.contains(gatewayId)) {
                        deviceConnectedClients.add(gatewayId);
                        s_logger.info("Added new client {} to connected Device list", gatewayId);
                    }
                    s_logger.info("DATA RECEIVED FROM GATEWAY {} with messageId:", gatewayId, message.getId());
                    ByteArrayInputStream bis = new ByteArrayInputStream(message.getPayload());
                    ObjectInput in = new ObjectInputStream(bis);
                    GatewayDataProtos.DevicesReadingsFromGateway gatewayReadingsProtobuf = (GatewayDataProtos.DevicesReadingsFromGateway) in.readObject();

                    DeviceReadingsFromGateway gatewayReadings = deserializer.deserializeDeviceReadingsFromGateway(gatewayReadingsProtobuf);
                    s_logger.info("Gateway timestamp {}", gatewayReadings.getGatewayTimestamp().toString());
                    for (DeviceData deviceData : gatewayReadings.getDeviceDataList()) {
                        s_logger.info("Device id {}: {}", deviceData.getDeviceId(), deviceData.getDeviceType());
                        s_logger.info("device data timestamp {}", deviceData.getTimestamp().toString());
                        for (DeviceReading reading : deviceData.getDeviceReadings()) {
                            s_logger.info("reading type {}", reading.getReadingType().toString());
                            s_logger.info("reading value: {}", reading.getValue());
                        }
                    }

                    String responseTopic = topic + "/response";

                    ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
                    String response = "Success " + message.getId();
                    s_logger.info("Sending response back to publisher on response topic {}. Response {}", responseTopic, response);
                    MqttMessage messageToSend = new MqttMessage(response.getBytes());

                    mqttClient.publish(responseTopic, messageToSend);
                    s_logger.info("Response sent on response topic {} successfully", responseTopic);
                });
            } else {
                s_logger.warn("Disconnected to the mqtt broker {}", s_mqttBroker);
                configureSubscriber();
            }

        } catch(MqttException e) {

        }
    }

    private  void subscribeToData() throws MqttException {
        while (true){

        }
    }

    public String getMqttClientId() {
        return mqttClientId;
    }

    public MqttClient getMqttClient() {
        return mqttClient;
    }

    public List<String> getDeviceConnectedClients() {
        return deviceConnectedClients;
    }

    public void cleanDeviceConnectedClients() {
        deviceConnectedClients.clear();
    }

    public void removeClient(String clientId){
        deviceConnectedClients.remove(clientId);
    }
}
