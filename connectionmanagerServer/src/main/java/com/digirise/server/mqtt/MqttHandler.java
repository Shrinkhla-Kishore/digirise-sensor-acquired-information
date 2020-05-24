package com.digirise.server.mqtt;

import com.digirise.server.mqtt.receiver.Subscriber;
import com.digirise.server.mqtt.receiver.SubscriberResponse;
import com.digirise.server.mqtt.sender.FirmwareDispatcher;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class MqttHandler implements Runnable{
    public static final Logger s_logger = LoggerFactory.getLogger(MqttHandler.class);

    @Value("${mqtt.broker}")
    private String s_mqttBroker;
    private FirmwareDispatcher firmwareDispatcher;
    ThreadPoolExecutor subscriberExecutor;
    private ThreadPoolExecutor subscribedTopicResponseExecutor;
    private String mqttClientId;
    private MqttClient mqttClient;
    @Autowired
    private Subscriber subscriber;
    @Autowired
    private SubscriberResponse subscriberResponse;
    private boolean stopped = false;

    public MqttHandler(FirmwareDispatcher firmwareDispatcher /*, @org.jetbrains.annotations.NotNull Subscriber subscriber*/) {
       // subscriber.configureMqtt();
    }

    @PostConstruct
    public void configure() {
        configureMqtt();
        subscriberExecutor = new ScheduledThreadPoolExecutor(1);
        ((ScheduledThreadPoolExecutor) subscriberExecutor).scheduleWithFixedDelay(this, 5000, 3000, TimeUnit.MILLISECONDS);
        subscribedTopicResponseExecutor = new ScheduledThreadPoolExecutor(1);
         ((ScheduledThreadPoolExecutor) subscribedTopicResponseExecutor).schedule(subscriberResponse, 5000, TimeUnit.MILLISECONDS);
    }

    public void configureMqtt() {
        try {
            mqttClientId = "serverApplication";
            mqttClient = new MqttClient(s_mqttBroker, mqttClientId);
        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void run() {
        if (!mqttClient.isConnected()) {
            try {
                //            Thread.currentThread().sleep(5000);
                mqttClient.connect();
                s_logger.info("Mqtt subscriber started. Connected to {}", s_mqttBroker);
                subscriber.setMqttClient(mqttClient);
                subscriber.setMqttClientId(mqttClientId);
                subscriber.subscribeGatewayDiscoveryTopic();
                subscriber.subscribeDeviceDataTopic();
                subscriberResponse.setMqttClient(mqttClient);
            } catch (MqttException e) {
                s_logger.warn("Error connecting to mqtt broker {}", s_mqttBroker);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
            }
        }
    }

    @PreDestroy
    public void destroy() {
        stopped =true;
        try {
            mqttClient.close();
            s_logger.debug("Closed MQTT client");
        } catch (MqttException e){
            s_logger.warn("Error in shutting down the mqtt client");
            s_logger.trace("{}", e.getStackTrace());
        }

        subscriberExecutor.shutdownNow();
        try {
            boolean result = subscriberExecutor.awaitTermination(30, TimeUnit.SECONDS);
            if (result) {
                s_logger.debug("Executor shutdown");
            } else {
                s_logger.warn("Failed to shutdown executor.");
            }
        } catch (InterruptedException ignore) {
            s_logger.warn("Interrupted while waiting for executor to shutdown.");
        }
    }


}
