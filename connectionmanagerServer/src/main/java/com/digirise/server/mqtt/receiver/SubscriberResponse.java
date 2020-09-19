package com.digirise.server.mqtt.receiver;

import com.digirise.server.handler.MqttMessageWrapper;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-15
 * Author: shrinkhlak
 */
@Component
public class SubscriberResponse implements Runnable {
    public static final Logger s_logger = LoggerFactory.getLogger(SubscriberResponse.class);
    private final LinkedBlockingQueue<MqttMessageWrapper> respQueue = new LinkedBlockingQueue<>();
    private boolean stopped = false;
    private MqttClient mqttClient;

    @Override
    public void run() {
        while (!stopped) {
            try {
                MqttMessageWrapper mqttMessageWrapper = null;
                mqttMessageWrapper = respQueue.poll(500, TimeUnit.MILLISECONDS);
                if (!stopped && mqttMessageWrapper != null) {
                    mqttClient.publish(mqttMessageWrapper.getTopic(), mqttMessageWrapper.getMqttMessage());
                }
            } catch (InterruptedException ignore) {
                s_logger.trace("Interrupted while polling the response respQueue.");
            }catch (Exception e) {
                s_logger.debug("Exception, will try to recover: {}", e.getMessage());
                s_logger.info("Exception, will try to recover. {}", e);
            } catch (Error e) {
                s_logger.error("Fatal error.{}", e);
                throw e;
            }
        }
    }

    public void schedule(MqttMessageWrapper task) {
        if (!stopped)
            respQueue.add(task);
        else
            s_logger.warn("No new tasks can be added to response queue as the application is being stopped");
    }

    public void stop() {
        stopped = true;
    }

    @PreDestroy
    public void destroy() {
        stopped = true;
        if (respQueue.size() > 0) {
            respQueue.forEach(mqttMessageWrapper -> {
                try {
                    mqttClient.publish(mqttMessageWrapper.getTopic(), mqttMessageWrapper.getMqttMessage());
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            });
        }
        respQueue.clear();
    }


    public void setMqttClient(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }
}
