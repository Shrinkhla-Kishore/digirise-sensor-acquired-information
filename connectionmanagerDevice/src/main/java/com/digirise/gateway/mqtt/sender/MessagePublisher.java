package com.digirise.gateway.mqtt.sender;

import com.digirise.gateway.mqtt.sender.serialization.DevicesReadingsFromGatewaySerializer;
import com.digirise.gateway.mqtt.sender.serialization.GatewayDiscoverySerializer;
import com.digirise.proto.GatewayDataProtos;
import com.digirise.proto.GatewayDiscoveryProtos;
import com.digirise.sai.commons.discovery.GatewayDiscovery;
import com.digirise.sai.commons.helper.DeviceReading;
import com.digirise.sai.commons.helper.ReadingType;
import com.digirise.sai.commons.readings.DeviceData;
import com.digirise.sai.commons.readings.DeviceReadingsFromGateway;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class MessagePublisher {
    private static final Logger s_logger = LoggerFactory.getLogger(MessagePublisher.class);
    @Autowired
    private DevicesReadingsFromGatewaySerializer devicesReadingsFromGatewaySerializer;
    @Autowired
    private GatewayDiscoverySerializer gatewayDiscoverySerializer;
    @Value("${gateway.name}")
    private String gatewayName;
    @Value("${gateway.customer.name}")
    private String gatewayCustomerName;
    @Value("${gateway.customer.id}")
    private String gatewayCustomerId;
    @Value("${gateway.location}")
    private String gatewayLocation;
    @Value("${gateway.coordinates}")
    private String gatewayCoordinates;
    private static final String SUFFIX_DATA_TOPIC = "/data/";
    private static final String SUFFIX_INFO_TOPIC = "/info/";
    private static final String PREFIX_TOPIC = "gateway/";
    private AtomicInteger alarmId;
    public final int qosLevel = 2;
   // private String gatewayId;
    private MqttClient mqttClient;
    private MqttConnectOptions options;

    public MessagePublisher() {
        alarmId = new AtomicInteger(40);
    }

    public void startPublisher(String mqttBroker/*, String gatewayId*/) {
        try {
       //     this.gatewayId = gatewayId;
            s_logger.info("creating publisher with gateway name {}", gatewayName);
            mqttClient = new MqttClient(mqttBroker, gatewayName);
            setConnectOptions();
            s_logger.info("Inside step 1");
            while (!mqttClient.isConnected()){
                try {
                    Thread.currentThread().sleep(5000);
                    mqttClient.connect();
                    s_logger.info("Mqtt client connected to {}", mqttBroker);
                } catch (MqttException e) {
                    s_logger.warn("Error connecting to mqtt broker {}", mqttBroker);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (mqttClient.isConnected()) {
                s_logger.info("MessagePublisher connected to MQTT broker {}", mqttBroker);
                String gatewayInfoTopicResp = PREFIX_TOPIC + gatewayName + SUFFIX_INFO_TOPIC + "+/response";
                mqttClient.subscribe(gatewayInfoTopicResp, new ResponseCallback());
                s_logger.info("MessagePublisher subscribed to topic {}", gatewayInfoTopicResp);
                String topicName = "gateway/" + gatewayName + "/data/+/response";
                mqttClient.subscribe(topicName, new ResponseCallback());
                // mqttClient.setCallback(new ResponseCallback());
                s_logger.info("MessagePublisher subscribed to topic {}", topicName);
            }
        } catch (MqttException e) {
            s_logger.warn("Error creating mqtt client for gateway id {}", gatewayName);
        }
    }

    public void sendData(String mqttBroker) throws MqttException, IOException {
        s_logger.info("Inside sendData");
        if (mqttClient.isConnected()) {
            if (checkIfDataToSend()) {
                UUID uuid = UUID.randomUUID();
                String alarm_topic = PREFIX_TOPIC + gatewayName + SUFFIX_DATA_TOPIC + uuid;

                List<DeviceData> fakeDevicesData = new ArrayList<>();
                //TO-DO: Handle real sensor data
                fakeDevicesData.add(createDeviceData());
                DeviceReadingsFromGateway deviceReadingsFromGateway = new DeviceReadingsFromGateway();
                deviceReadingsFromGateway.setCustomerName(gatewayCustomerName);
                deviceReadingsFromGateway.setGatewayName(gatewayName);
                deviceReadingsFromGateway.setDeviceDataList(fakeDevicesData);
                GatewayDataProtos.DevicesReadingsFromGateway gatewayReadings = devicesReadingsFromGatewaySerializer.serializeDevicesData(deviceReadingsFromGateway);
                publishInformation(gatewayReadings,alarm_topic);
            } else {
                s_logger.debug("No data to send from the connected devices");
            }
        } else {
            s_logger.warn("Mqtt gateway with Id {} not connected", gatewayName);
            startPublisher(mqttBroker);
        }
    }

    public void sendGatewayDiscoveryInfo() throws IOException, MqttException {
        GatewayDiscovery gatewayDiscovery = new GatewayDiscovery();
        s_logger.info("Sending gatewayDiscovery information");
        s_logger.info("containing: gatewayName {}, customerName {}, CustomerId {}, location {}, coordinates {}",
                gatewayName, gatewayCustomerName, gatewayCustomerId, gatewayLocation, gatewayCoordinates);
        gatewayDiscovery.setGatewayName(gatewayName);
        if(gatewayCustomerName != null && gatewayCustomerName.length() > 0)
            gatewayDiscovery.setCustomerName(gatewayCustomerName);
        if (gatewayCustomerId != null && gatewayCustomerId.trim().length() > 0){
            gatewayCustomerId = gatewayCustomerId.trim();
            s_logger.info("GatewayCustomerId is {}, and {}", gatewayCustomerId, Long.parseLong(gatewayCustomerId));
            gatewayDiscovery.setCustomerId(Long.parseLong(gatewayCustomerId));
        }
        gatewayDiscovery.setLocation(gatewayLocation);
        gatewayDiscovery.setCoordinates(gatewayCoordinates);
        //TODO: Check if there is any device connected
        GatewayDiscoveryProtos.GatewayDiscovery gatewayDiscoveryProto =
                gatewayDiscoverySerializer.serializeGatewayDiscovery(gatewayDiscovery);
        UUID uuid = UUID.randomUUID();
        String gatewayInfoTopic = PREFIX_TOPIC + gatewayName + SUFFIX_INFO_TOPIC + uuid;
        publishInformation(gatewayDiscoveryProto, gatewayInfoTopic);
    }

    private void publishInformation(Object protoObject, String topic) throws IOException, MqttException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(byteOutputStream);
        os.writeObject(protoObject);
        s_logger.info("ClientId {} publishing data on topic {}",
                mqttClient.getClientId(), topic);
        MqttMessage mqttMessage = new MqttMessage(byteOutputStream.toByteArray());
        mqttMessage.setQos(1);
        mqttMessage.setId(alarmId.get());
        mqttClient.publish(topic, mqttMessage);
    }

    private boolean checkIfDataToSend() {
        //TODO: Implement code to check if there is data to send
        return true;
    }

//    private ByteArrayOutputStream prepareDataToSend() throws IOException {
//        List<DeviceData> fakeDevicesData = new ArrayList<>();
//        //TO-DO: Handle real sensor data
//        fakeDevicesData.add(createDeviceData());
//        GatewayDataProtos.DevicesReadingsFromGateway gatewayReadings = devicesReadingsFromGatewaySerializer.serializeDevicesData(fakeDevicesData);
//        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
//        ObjectOutputStream os = new ObjectOutputStream(byteOutputStream);
//        os.writeObject(gatewayReadings);
//        return byteOutputStream;
//    }

    private DeviceData createDeviceData() {
        //TODO: TO be removed. Fake data
        DeviceData deviceData = new DeviceData();
        deviceData.setDeviceName(Integer.toString(alarmId.getAndIncrement()));
        deviceData.setTimestamp(new Timestamp(new Date().getTime()));
        DeviceReading reading = new DeviceReading();
        List<DeviceReading> deviceReadingList =  new ArrayList<>();
        reading.setReadingType(ReadingType.SENSOR_CURRENT_VALUE);
        reading.setValue(String.valueOf(alarmId.intValue() + 2));
        deviceReadingList.add(reading);
        deviceData.setDeviceReadings(deviceReadingList);
        s_logger.info("CREATED FAKE DEVICE DATA FOR DEVICE ID {}: {}.",
                deviceData.getDeviceName(), deviceData.getTimestamp());
        return deviceData;
    }

    public void closePublisher() {
        try {
            s_logger.info("Shutting down the mqtt gateway {}", gatewayName);
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
        options.setUserName(gatewayName);
        options.setKeepAliveInterval(60);
        options.setConnectionTimeout(300);
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
}
