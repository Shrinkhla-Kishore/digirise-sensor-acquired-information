package com.digirise.server.grpc.clientside;

import com.digirise.proto.CommonStructuresProto;
import com.digirise.sai.commons.helper.DeviceReading;
import com.digirise.sai.commons.helper.DeviceReadingsResponse;
import com.digirise.sai.commons.helper.DeviceType;
import com.digirise.sai.commons.readings.DeviceData;
import com.digirise.sai.commons.serializer.DeviceReadingsResponseSerializer;
import com.digirise.sai.commons.servercommunication.DeviceDataBetweenServers;
import com.digirise.server.handler.DatabaseHelper;
import com.digirise.server.handler.MqttMessageWrapper;
import com.digirise.server.model.Gateway;
import com.digirise.server.model.Sensor;
import com.digirise.server.mqtt.receiver.SubscriberResponse;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

/** DataProcessingHandler class is used to handle the data message received.
 * It checks and writes gateway information in db.
 * It creates the message to be sent to data processor server.
 * It creates the response to be sent to the gateway.
 *
 * It also maintains a queue of messages to be re-sent between the server and data processor server.
 *
 *
 * Created by IntelliJ IDEA.
 * Date: 2020-05-31
 * Author: shrinkhlak
 */

@Component
public class DataProcessingHandler implements Runnable {
    private static final Logger s_logger = LoggerFactory.getLogger(DataProcessingHandler.class);
    @Autowired
    private SubscriberResponse subscriberResponse;
    @Autowired
    private DeviceReadingsResponseSerializer responseToGatewaySerializer;
    @Autowired
    private DatabaseHelper databaseHelper;
    private ExecutorService dpExecutorService;
    private ExecutorService messageResendExecutorMaster;
    private ExecutorService messageResendExecutor;
    private Queue<DataProcessingMessageHandler> dpMessagesToBeResent;

    public DataProcessingHandler() {
        dpMessagesToBeResent = new LinkedBlockingQueue<>();
        dpExecutorService = Executors.newFixedThreadPool(10);
        messageResendExecutorMaster = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r, "resend-masterThread");
                return thread;
            }
        });
        messageResendExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r, "resend-thread1");
                return thread;
            }
        });
        ((ScheduledExecutorService) messageResendExecutorMaster).scheduleWithFixedDelay(this, 5, 10, TimeUnit.SECONDS);
    s_logger.trace("Instance created");
    }

    public void addDpMessagesToResend(DataProcessingMessageHandler dataProcessingMessageHandler){
        if (!dpMessagesToBeResent.contains(dataProcessingMessageHandler)) {
            dpMessagesToBeResent.add(dataProcessingMessageHandler);
            s_logger.trace("Messages for response topic {} added to re-sent later. Size of queue is {}",
                    dataProcessingMessageHandler.getResponseTopic(), dpMessagesToBeResent.size());
        }
        else
            s_logger.trace("Message for response topic {} already added. Size of queue is {}",
                    dataProcessingMessageHandler.getResponseTopic(), dpMessagesToBeResent.size());
    }

    @Override
    public void run() {
        while (dpMessagesToBeResent.size() > 0) {
            DataProcessingMessageHandler messageHandler = dpMessagesToBeResent.poll();
            s_logger.trace("Found a message to send");
            s_logger.info("Response topic {} to re-send", messageHandler.getResponseTopic());
            messageHandler.sendDeviceReadingsToDP();
        }
        s_logger.trace("No messages to handle, queue size {}", dpMessagesToBeResent.size());
    }

    public Gateway updateDevicesAttachedToGatewayInDatabase(List<DeviceData> deviceDataList, String gatewayName, String customerName) {
        s_logger.info("Checking if all devices/sensors are added to DB. Device list size {}", deviceDataList.size());

        List<String> deviceNames = new ArrayList<>();
        for (DeviceData deviceData : deviceDataList){
            deviceNames.add(deviceData.getDeviceName());
        }
        s_logger.trace("Get gateway from db for gateway name {} and customer {}", gatewayName, customerName);
        Gateway gateway = databaseHelper.getGatewayFromNameAndCustomerName(gatewayName, customerName);
        s_logger.trace("Gateway found. {}", gateway.toString());
        boolean newDeviceFound = databaseHelper.saveDevicesForGateway(gateway, deviceNames);
        boolean gatewayDiscoveryRequired = newDeviceFound;
        if (gatewayDiscoveryRequired == true && gateway.isDiscoveryRequired() != true) {
            gateway.setDiscoveryRequired(gatewayDiscoveryRequired);
            databaseHelper.updateGateway(gateway);
        }/* else if (gateway.isDiscoveryRequired() == true) {
            gatewayDiscoveryRequired = true;
        }*/
        return gateway;
    }

    public List<DeviceDataBetweenServers> createDeviceReadingsBetweenServer(List<DeviceData> deviceDataList, Gateway gateway) {
        List<DeviceDataBetweenServers> deviceDataBetweenServersList = new ArrayList<>();
        for (DeviceData deviceData : deviceDataList){
            Sensor sensor = databaseHelper.findSensorByNameAndGateway(deviceData.getDeviceName(), gateway);
            DeviceDataBetweenServers deviceDataBetweenServers = new DeviceDataBetweenServers();
            deviceDataBetweenServers.setDeviceId(sensor.getSensorId());
            String measurementUnit = "--";
            if (sensor.getType() == DeviceType.TEMPERATURE_SENSOR) {
                measurementUnit = "degree";
            } else if (sensor.getType() == DeviceType.HUMIDITY_SENSOR) {
                measurementUnit = "mg/L";
            } else if (sensor.getType() == DeviceType.MOTION_SENSOR) {
                measurementUnit = "cm"; //TODO: This needs to be checked and refined.
            }
            for (DeviceReading deviceReading : deviceData.getDeviceReadings()) {
                deviceReading.setUnit(measurementUnit);
            }
            deviceDataBetweenServers.setDeviceReadings(deviceData.getDeviceReadings());
            deviceDataBetweenServers.setTimestamp(deviceData.getTimestamp());
            deviceDataBetweenServersList.add(deviceDataBetweenServers);
        }
        return deviceDataBetweenServersList;
    }

    public void sendResponseToGateway(DeviceReadingsResponse responseFromDPDeserialized, String responseTopic) {
        // Send response back to the gateway
        CommonStructuresProto.DeviceReadingsResponse readingResponseProto =
                responseToGatewaySerializer.serialize(responseFromDPDeserialized);
        s_logger.info("Response {} serialized to be sent to gateway. Sending response back on response topic {}",
                readingResponseProto.getStatus(), responseTopic);
        try {
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(byteOutputStream);
            os.writeObject(readingResponseProto);
            MqttMessage messageToSend = new MqttMessage(byteOutputStream.toByteArray());
            MqttMessageWrapper mqttMessageWrapper = new MqttMessageWrapper();
            mqttMessageWrapper.setTopic(responseTopic);
            mqttMessageWrapper.setMqttMessage(messageToSend);
            subscriberResponse.schedule(mqttMessageWrapper);
        } catch (IOException e) {
        }
    }

    public ExecutorService getDpExecutorService() {
        return dpExecutorService;
    }
}

