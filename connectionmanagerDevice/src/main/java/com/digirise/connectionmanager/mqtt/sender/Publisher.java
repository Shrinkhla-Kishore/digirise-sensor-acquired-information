package com.digirise.connectionmanager.mqtt.sender;

import com.digirise.api.GatewayDataProtos;
import com.digirise.connectionmanager.mqtt.sender.serialization.DevicesReadingsFromGatewaySerializer;
import com.digirise.sai.commons.dataobjects.DeviceData;
import com.digirise.sai.commons.dataobjects.DeviceReading;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * Date: 2019-02-24
 * Author: shrinkhlak
 */

@Component
public class Publisher {
    private static final Logger s_logger = LoggerFactory.getLogger(Publisher.class);

    @Autowired
    private DevicesReadingsFromGatewaySerializer devicesReadingsFromGatewaySerializer;
    private static final String SUFFIX_TOPIC = "/data/";
    private static final String PREFIX_TOPIC = "gateway/";
    private AtomicInteger alarmId;
    public final int qosLevel = 2;
    private String gatewayId;
    private MqttClient mqttClient;
    private MqttConnectOptions options;

    public Publisher() {
        alarmId = new AtomicInteger(40);
    }

//    public Publisher(String gatewayId) {
//        this.gatewayId = gatewayId;
//    }

    public void startPublisher(String mqttBroker, String gatewayId) {
        try {
            this.gatewayId = gatewayId;
            s_logger.info("creating publisher with gatewayId {}", gatewayId);
            mqttClient = new MqttClient(mqttBroker, "gateway-" + gatewayId);
            setConnectOptions();
            s_logger.info("Inside step 1");
            while (!mqttClient.isConnected()){
                try {
                    Thread.currentThread().sleep(2000);
                    mqttClient.connect();
                    s_logger.info("Mqtt client connected to {}", mqttBroker);
                } catch (MqttException e) {
                    s_logger.warn("Error connecting to mqtt broker {}", mqttBroker);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (mqttClient.isConnected()) {
                s_logger.info("Publisher connected to MQTT broker {}", mqttBroker);
                String topicName = "gateway/" + gatewayId + "/data/+/response";
                mqttClient.subscribe(topicName, new ResponseCallback());
                // mqttClient.setCallback(new ResponseCallback());
                s_logger.info("Publisher subscribed to topic {}", topicName);
            }
        } catch (MqttException e) {
            s_logger.warn("Error creating mqtt client for gateway id {}", gatewayId);
        }
    }

    public void sendData(String mqttBroker) throws MqttException, IOException {
        s_logger.info("Inside sendData");
        if (mqttClient.isConnected()) {
            UUID uuid = UUID.randomUUID();
            String alarm_topic = PREFIX_TOPIC + gatewayId + SUFFIX_TOPIC + uuid;

            List<DeviceData> fakeDevicesData = new ArrayList<>();
            //TO-DO: Handle real sensor data
            fakeDevicesData.add(createDeviceData());
            GatewayDataProtos.DevicesReadingsFromGateway gatewayReadings = devicesReadingsFromGatewaySerializer.serializeDevicesData(fakeDevicesData);
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(byteOutputStream);
            os.writeObject(gatewayReadings);

            s_logger.info("ClientId {} publishing data on topic {}",
                    mqttClient.getClientId(), alarm_topic);
//            MqttMessage mqttMessage = new MqttMessage(Integer.toString(alarmId.get()).getBytes());
            MqttMessage mqttMessage = new MqttMessage(byteOutputStream.toByteArray());


            mqttMessage.setQos(1);
            mqttMessage.setId(alarmId.get());
            mqttClient.publish(alarm_topic, mqttMessage);
        } else {
            s_logger.warn("Mqtt gateway with Id {} not connected", gatewayId);
            startPublisher(mqttBroker, gatewayId);
        }
    }

    private DeviceData createDeviceData() {
        DeviceData deviceData = new DeviceData();
        deviceData.setDeviceId(Integer.toString(alarmId.getAndIncrement()));
        deviceData.setDeviceType(GatewayDataProtos.DeviceType.TEMPERATURE_SENSOR);
        deviceData.setTimestamp(new Timestamp(new Date().getTime()));
        DeviceReading reading = new DeviceReading();
        List<DeviceReading> deviceReadingList =  new ArrayList<>();
        reading.setReadingType(GatewayDataProtos.ReadingType.SENSOR_CURRENT_VALUE);
        reading.setValue(String.valueOf(alarmId.intValue() + 2));
        deviceReadingList.add(reading);
        deviceData.setDeviceReadings(deviceReadingList);
        s_logger.info("CREATED FAKE DEVICE DATA FOR DEVICE ID {}: {},{}.",
                deviceData.getDeviceId(), deviceData.getDeviceType(), deviceData.getTimestamp());
        return deviceData;
    }

    public void closePublisher() {
        try {
            s_logger.info("Shutting down the mqtt gateway {}", gatewayId);
            mqttClient.disconnect();
            mqttClient.close();
        } catch (MqttException e) {
            s_logger.trace("Exception received while closing the mqtt client");
        }
    }

    private void setConnectOptions() {
        s_logger.info("Setting the connect options");
        options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setUserName(gatewayId);
        options.setKeepAliveInterval(60);
      //  options.setAutomaticReconnect(true);
      //  options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        try {
//            options.setSocketFactory(SslUtil.getSocketFactory("C:\\Users\\u4015811\\mosquitto_auth\\keys\\ca.crt",
//                    "C:\\Users\\u4015811\\mosquitto_auth\\keys\\client.crt",
//                    "C:\\Users\\u4015811\\mosquitto_auth\\keys\\client.key", ""));
        } catch (Exception e) {
            s_logger.error("Error in setting the socket factory");
            e.printStackTrace();
        }
    }

//    public String getGatewayId() {
//        return gatewayId;
//    }
//
//    public MqttClient getMqttClient() {
//        return mqttClient;
//    }
}
