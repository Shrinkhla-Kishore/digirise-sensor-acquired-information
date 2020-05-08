package com.digirise.gateway.mqtt.receiver;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class Subscriber {
    private static final Logger s_logger = LoggerFactory.getLogger(Subscriber.class);
    private static final String s_mqttBroker = "tcp://localhost:1884";
    private String clientId;
    private MqttClient mqttClient;

    public Subscriber() {
        try {
            clientId = "subscriber";
            mqttClient = new MqttClient(s_mqttBroker, clientId);
           // mqttClient.setCallback(new SubscriberCallback());
            mqttClient.connect();
         //   mqttClient.subscribe("/device/+/alarm");
            mqttClient.subscribe("/gateway/+/firmware", (topic, message) -> {
                s_logger.info("Sending response back to publisher on topic {}", topic);
                // Reading the client Id
                int lastIndex = topic.indexOf("-");
                String clientId = topic.substring("/device/".length(), lastIndex);
                String responseTopic = "/device/firmwareresponse/" + clientId;
              //  MqttTopic mqttTopic = mqttClient.getTopic(responseTopic);
                ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
                String response = "Success " + message.getId();
                s_logger.info("Sending response back to publisher client id {}. Response {}", clientId, response);
                MqttMessage messageToSend = new MqttMessage(response.getBytes());
               // mqttTopic.publish(messageToSend);
                mqttClient.publish(responseTopic, messageToSend);
                s_logger.info("Response sent by subscriber to clientID {]", clientId);
            });
        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public String getClientId() {
        return clientId;
    }

    public MqttClient getMqttClient() {
        return mqttClient;
    }
}
