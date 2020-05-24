package com.digirise.gateway.mqtt.sender;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@PropertySource("classpath:application.properties")
public class MessagePublisherManager {
    private static final Logger s_logger = LoggerFactory.getLogger(MessagePublisherManager.class);
    @Value("${mqtt.broker}")
    public String mqttBroker;
    @Autowired
    private MessagePublisher publisher;

    private AtomicInteger clientId;
    private List<MessagePublisher> publishersList;

    public void startMqttBroker() {
        s_logger.info("Inside startMqttBroker, Mqtt broker is {}", mqttBroker);
        clientId = new AtomicInteger(1);
        publishersList = new ArrayList<>();
        for (int i=1; i<=1; i++) {
            String cltId = String.valueOf(this.clientId.getAndIncrement());
            //MessagePublisher publisher = new MessagePublisher(cltId);
            publisher.startPublisher(mqttBroker);
            s_logger.info("created publisher");
            publishersList.add(publisher);
            s_logger.info("MessagePublisher added");
        }
    }

//    @Override
//    public void run() {
    public void startPublish() {
        try {
            s_logger.info("publishing gateway information");
            publishGatewayInfo();
            s_logger.info("Starting to publish alarms");
            while (true) {
                publishAlarm();
                Thread.sleep(5000);
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

    private void publishGatewayInfo() throws IOException, MqttException {
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
}
