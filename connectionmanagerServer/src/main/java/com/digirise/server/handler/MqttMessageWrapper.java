package com.digirise.server.handler;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-15
 * Author: shrinkhlak
 */
public class MqttMessageWrapper {
    private MqttMessage mqttMessage;
    private String topic;

    public MqttMessage getMqttMessage() {
        return mqttMessage;
    }

    public void setMqttMessage(MqttMessage mqttMessage) {
        this.mqttMessage = mqttMessage;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
