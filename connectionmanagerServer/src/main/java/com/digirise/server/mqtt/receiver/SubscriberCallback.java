package com.digirise.server.mqtt.receiver;

import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;

//public class SubscriberCallback implements MqttCallback {
public class SubscriberCallback implements IMqttMessageListener {
    private static final Logger s_logger = LoggerFactory.getLogger(SubscriberCallback.class);
    private static final String s_mqttBroker = "tcp://localhost:1884";
    //@Override
    public void connectionLost(Throwable throwable) {
        s_logger.info("Connection lost with the MQTT broker...try reconnecting");
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        s_logger.info("messageArrived. Received message {} from {}", mqttMessage.toString(), s);
//        s_logger.info("handling response from {}", s);
//        Integer receivedValue = Integer.parseInt(mqttMessage.toString());
//        s_logger.info("Received message {}", receivedValue.intValue());
    }

    //@Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        try {
            s_logger.info("deliveryComplete method. Received message {}", iMqttDeliveryToken.getMessage().toString());
            sendResponse(iMqttDeliveryToken);
        } catch (MqttException e) {
            s_logger.info("Exception received is {}", e.getMessage());
            s_logger.trace("{}", e.getStackTrace());
        }
    }

    private void sendResponse(IMqttDeliveryToken mqttDeliveryToken) throws MqttException {
        String clientId = mqttDeliveryToken.getClient().getClientId();
        MqttClient mqttClient = new MqttClient(s_mqttBroker, clientId);
        mqttClient.connect();
        String responseTopic = "/device/alarmresponse/" + clientId;
        MqttTopic mqttTopic = mqttClient.getTopic(responseTopic);
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        String response = "Success " + mqttDeliveryToken.getMessageId();
        MqttMessage messageToSend = new MqttMessage(response.getBytes());
        mqttTopic.publish(messageToSend);
    }
}
