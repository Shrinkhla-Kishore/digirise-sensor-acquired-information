package com.digirise.gateway.mqtt;

import com.digirise.gateway.mqtt.sender.MessagePublisherManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

@Component
public class ClientHandler {
    private static final Logger s_logger = LoggerFactory.getLogger(ClientHandler.class);
    @Autowired
    private MessagePublisherManager messageDispatcher;

    @PostConstruct
    public void startMqtt() {
        s_logger.info("Starting the Client handler");
        try {
            Thread.currentThread().sleep(5000);
        } catch (InterruptedException e) {
            s_logger.warn("The thread was interrupted");
            System.exit(1);
        }
        messageDispatcher.startMqttBroker();
        s_logger.info("After configuring the mqtt client :)");
        messageDispatcher.startPublishMessage();
        s_logger.info("After starting the publish of mqtt messages :)");
    }
}
