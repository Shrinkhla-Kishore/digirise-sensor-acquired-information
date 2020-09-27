package com.digirise.gateway.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

@Component
public class GatewayMqttInitiator {
    private static final Logger s_logger = LoggerFactory.getLogger(GatewayMqttInitiator.class);
    @Autowired
    private MqttMessageConnectionManager messagePublisherManager;

    @PostConstruct
    public void startMqtt() {
        s_logger.info("Starting the Client handler");
        try {
            Thread.currentThread().sleep(5000);
        } catch (InterruptedException e) {
            s_logger.warn("The thread was interrupted");
            System.exit(1);
        }
        messagePublisherManager.startMqttBroker();
        s_logger.info("After configuring the mqtt client :)");
        messagePublisherManager.startPublishMessage();
        s_logger.info("After starting the publish of mqtt messages :)");
    }
}
