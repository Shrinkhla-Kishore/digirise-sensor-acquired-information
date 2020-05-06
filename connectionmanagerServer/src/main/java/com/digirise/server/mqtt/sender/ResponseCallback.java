//package com.getinge.server.mqtt.sender;
//
//import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
//import org.eclipse.paho.client.mqttv3.MqttCallback;
//import org.eclipse.paho.client.mqttv3.MqttException;
//import org.eclipse.paho.client.mqttv3.MqttMessage;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class ResponseCallback implements MqttCallback {
//    private static final Logger s_logger = LoggerFactory.getLogger(ResponseCallback.class);
//    private static final String s_mqttBroker = "tcp://localhost:1884";
//
//    @Override
//    public void connectionLost(Throwable throwable) {
//
//    }
//
//    @Override
//    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
//        s_logger.info("Response received by publisher {}", s);
//    }
//
//    @Override
//    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
//        try {
//            s_logger.info("deliveryComplete() response received by the publisher {}", iMqttDeliveryToken.getMessage().toString());
//          //  s_logger.info("deliveryComplete() response received by the publisher");
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
//    }
//}
