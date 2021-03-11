package com.digirise.gateway.mqtt.sender;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MqttConnectionManager class is used to read the mqtt broker url and start the mqtt broker by calling
 * the @MessagePublisher class.
 * Currently it initiates a gateway discovery message to be sent on the system start-up.
 * Currently it sends one gateway data message per second. This should be handled correctly in future.
 */

@Component
public class MqttConnectionManager {
    private static final Logger s_logger = LoggerFactory.getLogger(MqttConnectionManager.class);
    @Value("${mqtt.broker}")
    public String mqttBroker;
    @Autowired
    private MessagePublisher messagePublisher;

    private AtomicInteger clientId;
    private List<MessagePublisher> publishersList;

    @PostConstruct
    public void startMqtt() {
        s_logger.info("Starting the Client handler");
        try {
            Thread.currentThread().sleep(5000);
        } catch (InterruptedException e) {
            s_logger.warn("The thread was interrupted");
            System.exit(1);
        }
        this.startMqttBroker();
        s_logger.info("After configuring the mqtt client :)");
        this.startPublishMessage();
        s_logger.info("After starting the publish of mqtt messages :)");
    }

    private void startMqttBroker() {
        s_logger.info("Inside startMqttBroker, Mqtt broker is {}", mqttBroker);
        clientId = new AtomicInteger(1);
        publishersList = new ArrayList<>();
        for (int i=1; i<=1; i++) {
            messagePublisher.startMqttPublisher(mqttBroker);
            s_logger.info("created messagePublisher");
            publishersList.add(messagePublisher);
            s_logger.info("MessagePublisher added");
        }
    }

    private void startPublishMessage() {
        try {
            s_logger.info("publishing gateway information");
            publishGatewayDiscoveryInfo();
            s_logger.info("Starting to publish alarms");
            while (true) {
                publishAlarm();
                Thread.sleep(1000); //should be 2000
            }
        }catch (MqttException e) {
            s_logger.warn("{}", e.getMessage());
            s_logger.trace("{}", e.getStackTrace());
            shutdownClient();
        } catch (InterruptedException e) {
            s_logger.error("{}", e.getMessage());
            s_logger.trace("{}", e.getStackTrace());
            shutdownClient();
        } catch (IOException e){
            s_logger.error("{}", e.getMessage());
            s_logger.trace("{}", e.getStackTrace());
            shutdownClient();
        }
    }

    private void publishGatewayDiscoveryInfo() throws IOException, MqttException {
        for(MessagePublisher publisher : publishersList) {
            publisher.sendGatewayDiscoveryInfo();
        }
    }

    private void publishAlarm() throws MqttException, IOException {
        for (MessagePublisher publisher : publishersList) {
            publisher.sendData(mqttBroker);
        }
    }

    private void shutdownClient() {
        s_logger.info("Shutting down publishers ...");
        for (MessagePublisher publisher : publishersList) {
            publisher.closePublisher();
        }
    }

    public List<MessagePublisher> getPublisherList(){
            return publishersList;
        }

    public void setMqttBroker(String mqttBroker) {
        this.mqttBroker = mqttBroker;
    }
}
