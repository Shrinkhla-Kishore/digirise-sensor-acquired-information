package com.digirise.connectionmanager.mqtt;

import com.digirise.connectionmanager.mqtt.sender.MessageDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClientHandler {
    private static final Logger s_logger = LoggerFactory.getLogger(ClientHandler.class);

    @Autowired
    public ClientHandler(MessageDispatcher messageDispatcher) {
        s_logger.info("Starting the thread pool to send request to Mqtt broker every 2 seconds");
        try {
            Thread.currentThread().sleep(5000);
        } catch (InterruptedException e) {
            s_logger.warn("The thread was interrupted");
            System.exit(1);
        }
        messageDispatcher.configureMessageDisptacher();
        s_logger.info("After configuring the mqtt client :)");
        messageDispatcher.startPublish();
        s_logger.info("After starting the publish of mqtt messages :)");
    }
}
