package com.digirise.connectionmanager.mqtt.sender;

import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//public class ResponseCallback implements MqttCallback {
public class ResponseCallback implements IMqttMessageListener {
    private static final Logger s_logger = LoggerFactory.getLogger(ResponseCallback.class);
  //  private static final String mqttBroker = "tcp://localhost:1884";

    // @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        s_logger.info("Response received by publisher {}", s);
    }

    // @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        try {
            s_logger.info("deliveryComplete() response received by the publisher {}", iMqttDeliveryToken.getMessage().toString());
          //  s_logger.info("deliveryComplete() response received by the publisher");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
