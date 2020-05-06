//package com.getinge.server.mqtt.sender;
//
//import org.eclipse.paho.client.mqttv3.MqttClient;
//import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
//import org.eclipse.paho.client.mqttv3.MqttException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * Created by IntelliJ IDEA.
// * Date: 2019-02-24
// * Author: shrinkhlak
// */
//
//public class Publisher {
//    private static final Logger s_logger = LoggerFactory.getLogger(Publisher.class);
//    public static final String s_mqttBroker = "tcp://localhost:1884";
//    public final int qosLevel = 2;
//
//    private String clientId;
//    private MqttClient mqttClient;
//    private MqttConnectOptions options;
//
//    public Publisher(String clientId) {
//        try {
//            this.clientId = clientId + "-pub";
//            s_logger.info("creating publisher with clientId {}", clientId);
//            mqttClient = new MqttClient(s_mqttBroker, clientId);
//            s_logger.info("At step 1");
//            setConnectOptions();
//            s_logger.info("At step 2");
//            mqttClient.connect();
//            s_logger.info("At step 3");
//            mqttClient.subscribe("/device/alarmresponse/" + clientId);
//            s_logger.info("At step 4");
//           // mqttClient.setCallback(new ResponseCallback());
//            s_logger.info("Publisher created");
//        } catch (MqttException e) {
//            e.printStackTrace();
//            System.exit(1);
//        }
//    }
//
//    public void closePublisher() {
//        try {
//            mqttClient.disconnect();
//            mqttClient.close();
//        } catch (MqttException e) {
//            s_logger.trace("Exception received while closing the mqtt client");
//        }
//    }
//
//    private void setConnectOptions() {
//        options = new MqttConnectOptions();
//        options.setCleanSession(false);
//    }
//
//    public String getMqttClientId() {
//        return clientId;
//    }
//
//    public MqttClient getMqttClient() {
//        return mqttClient;
//    }
//}
