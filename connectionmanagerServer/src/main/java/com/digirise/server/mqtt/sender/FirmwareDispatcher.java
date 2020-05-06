package com.digirise.server.mqtt.sender;

import com.digirise.server.mqtt.receiver.Subscriber;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class FirmwareDispatcher implements Runnable {
    private static final Logger s_logger = LoggerFactory.getLogger(FirmwareDispatcher.class);
    private static final String s_mqttBroker = "tcp://localhost:1884";
    private static final String BASE_TOPIC = "/device/";
    private MqttClient mqttFirmwareClient;
    private AtomicInteger alarmId;
    @Autowired
    private Subscriber subscriber;


    public FirmwareDispatcher() {
        try {
            s_logger.info("Inside FirmwareDispatcher constructor");
            mqttFirmwareClient = new MqttClient(s_mqttBroker, "firmwareClient");
            mqttFirmwareClient.connect();
        } catch (MqttException e) {
            s_logger.debug("Error creating mqtt client \"firmwareClient\"");
            s_logger.trace("{}", e.getStackTrace());
        }
    }

    @Override
    public void run() {
        while(true) {
            try {
                pushFirmwareToDevices();
                Thread.sleep(10000);
            } catch (MqttException e) {
                s_logger.warn("{}", e.getMessage());
                s_logger.trace("{}", e.getStackTrace());
            } catch (InterruptedException e) {
                s_logger.error("{}", e.getMessage());
                s_logger.trace("{}", e.getStackTrace());
            }
        }
    }

    private void pushFirmwareToDevices() throws MqttException {
        s_logger.info("Checking for devices that are connected in order to push firmware updates");
        for (String clientId : subscriber.getDeviceConnectedClients()){
            String firmwareMessage = new String("Firmware sent for client " + clientId);
            MqttMessage mqttMessage = new MqttMessage(firmwareMessage.getBytes());
            mqttMessage.setQos(1);
            String firmwareTopic = new String(BASE_TOPIC + clientId + "/firmware");
            mqttFirmwareClient.publish(firmwareTopic, mqttMessage);
            s_logger.info("***Firmware pushed to client {} ***", clientId);
            subscriber.removeClient(clientId);
        }
        s_logger.info("Firmware updates pushed to {} clients", subscriber.getDeviceConnectedClients().size());
        subscriber.cleanDeviceConnectedClients();
    }

}
