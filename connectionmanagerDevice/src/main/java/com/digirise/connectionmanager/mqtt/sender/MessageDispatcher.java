package com.digirise.connectionmanager.mqtt.sender;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@PropertySource("classpath:application.properties")
public class MessageDispatcher /*implements Runnable */{
    private static final Logger s_logger = LoggerFactory.getLogger(MessageDispatcher.class);
    @Value("${mqtt.broker}")
    public String mqttBroker;
    @Autowired
    private Publisher publisher;
//    @Autowired
//    private Environment environment;

    private AtomicInteger clientId;
    private List<Publisher> publishersList;

    public MessageDispatcher() {
        s_logger.info("Inside messageDispatcher constructor. Mqtt broker is {}", mqttBroker);
        clientId = new AtomicInteger(1);
        publishersList = new ArrayList<>();
    }

    public void configureMessageDisptacher() {
        s_logger.info("Inside configureMessageDisptacher, Mqtt broker is {}", mqttBroker);
        for (int i=1; i<=1; i++) {
            String cltId = String.valueOf(this.clientId.getAndIncrement());
            //Publisher publisher = new Publisher(cltId);
            publisher.startPublisher(mqttBroker, cltId);
            s_logger.info("created publisher with id {}", cltId);
            publishersList.add(publisher);
            s_logger.info("Publisher {} added", cltId);
        }
    }

//    @Override
//    public void run() {
    public void startPublish() {
        s_logger.info("Starting to publish alarms");
        while(true) {
            try {
                publishAlarm();
                Thread.sleep(5000);
            } catch (MqttException e) {
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
    }

    private void publishAlarm() throws MqttException, IOException {
        for (Publisher publisher : publishersList) {
            publisher.sendData(mqttBroker);
        }
    }

    private void shutdownClient() {
        s_logger.info("Shutting down publishers ...");
        for (Publisher publisher : publishersList) {
            publisher.closePublisher();
        }
    }

    public List<Publisher> getPublisherList(){
            return publishersList;
        }
}
