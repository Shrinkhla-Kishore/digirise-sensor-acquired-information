package com.digirise.connectionmanager.mqtt;

import com.digirise.connectionmanager.mqtt.sender.MessageDispatcher;
import com.digirise.connectionmanager.mqtt.sender.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class ClientHandler {
    private static final Logger s_logger = LoggerFactory.getLogger(ClientHandler.class);
    ThreadPoolExecutor executor;

    @Autowired
    public ClientHandler(MessageDispatcher messageDispatcher) {
        executor = new ScheduledThreadPoolExecutor(1);
        s_logger.info("Starting the thread pool to send request to Mqtt broker every 2 seconds");
        messageDispatcher.configureMessageDisptacher();
        s_logger.info("After configuring the mqtt client :)");
        messageDispatcher.startPublish();
        s_logger.info("After starting the publish of mqtt messages :)");
    //    for (Publisher publisher : messageDispatcher.getPublisherList())
    //        ((ScheduledThreadPoolExecutor) executor).schedule(messageDispatcher, 10, TimeUnit.SECONDS);
    }
}
