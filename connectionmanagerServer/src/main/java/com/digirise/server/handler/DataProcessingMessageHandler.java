package com.digirise.server.handler;

import com.digirise.proto.CommnStructuresProtos;
import com.digirise.proto.GatewayDataForDpProtos;
import com.digirise.proto.GatewayDataProtos;
import com.digirise.proto.GatewaySensorReadingsServiceGrpc;
import com.digirise.sai.commons.helper.DeviceReading;
import com.digirise.sai.commons.helper.DeviceReadingsResponse;
import com.digirise.sai.commons.helper.DeviceType;
import com.digirise.sai.commons.helper.ResponseStatus;
import com.digirise.sai.commons.readings.DeviceData;
import com.digirise.sai.commons.readings.DeviceReadingsFromGateway;
import com.digirise.sai.commons.servercommunication.DeviceDataBetweenServers;
import com.digirise.sai.commons.servercommunication.DeviceReadingsBetweenServers;
import com.digirise.server.model.Gateway;
import com.digirise.server.model.Sensor;
import com.digirise.server.mqtt.receiver.SubscriberResponse;
import com.digirise.server.mqtt.receiver.deserialize.DeviceReadingsFromGatewayDeserializer;
import com.digirise.server.mqtt.receiver.deserialize.DeviceReadingsResponseDeserializer;
import com.digirise.server.mqtt.receiver.serialize.DeviceReadingsResponseSerializer;
import com.digirise.server.mqtt.receiver.serialize.DevicesReadingsBetweenServersSerializer;
import io.grpc.ManagedChannel;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-14
 * Author: shrinkhlak
 */
public class DataProcessingMessageHandler implements Runnable{
    public static final Logger s_logger = LoggerFactory.getLogger(DataProcessingMessageHandler.class);
    private Gateway gateway;
    private String gatewayName;
    private String customerName;
    private ManagedChannel managedChannel;
    private MqttClient mqttClient;
    private String responseTopic;
    private DeviceReadingsFromGatewayDeserializer deviceReadingsFromGatewayDeserializer;
    private DevicesReadingsBetweenServersSerializer devicesReadingsBetweenServersSerializer;
    private DeviceReadingsResponseDeserializer deviceReadingsResponseDeserializer;
    private DatabaseHelper databaseHelper;
    private DeviceReadingsResponseSerializer responseToGatewaySerializer;
    private SubscriberResponse subscriberResponse;
    private boolean gatewayDiscoveryRequired;
    private GatewayDataProtos.DevicesReadingsFromGateway devicesReadingsFromGatewayProto;

    public DataProcessingMessageHandler(ManagedChannel managedChannel, MqttClient mqttClient, String topic, DatabaseHelper databaseHelper, DeviceReadingsFromGatewayDeserializer deviceReadingsFromGatewayDeserializer,
                                        DevicesReadingsBetweenServersSerializer devicesReadingsBetweenServersSerializer, DeviceReadingsResponseDeserializer deviceReadingsResponseDeserializer,
                                        DeviceReadingsResponseSerializer responseToGatewaySerializer, SubscriberResponse subscriberResponse, GatewayDataProtos.DevicesReadingsFromGateway devicesReadingsFromGatewayProto) {
        this.managedChannel = managedChannel;
        this.mqttClient = mqttClient;
        this.responseTopic = topic + "/response";
        this.databaseHelper = databaseHelper;
        this.deviceReadingsFromGatewayDeserializer = deviceReadingsFromGatewayDeserializer;
        this.devicesReadingsBetweenServersSerializer = devicesReadingsBetweenServersSerializer;
        this.deviceReadingsResponseDeserializer = deviceReadingsResponseDeserializer;
        this.responseToGatewaySerializer = responseToGatewaySerializer;
        this.subscriberResponse = subscriberResponse;
        this.devicesReadingsFromGatewayProto = devicesReadingsFromGatewayProto;
        gatewayDiscoveryRequired = false;
    }

    @Override
    public void run() {
        // Deserialize the device readings
        DeviceReadingsFromGateway deviceReadingsFromGateway = deviceReadingsFromGatewayDeserializer.deserializeDeviceReadingsFromGateway(devicesReadingsFromGatewayProto);
        gatewayName = deviceReadingsFromGateway.getGatewayName();
        customerName = deviceReadingsFromGateway.getCustomerName();

        // Add the devices to the database, if not there already
        if (deviceReadingsFromGateway.getDeviceDataList().size() > 0)
            updateDevicesAttachedToGatewayInDatabase(deviceReadingsFromGateway.getDeviceDataList());

        //Create new DeviceReadingsBetweenServer object and send to DP application
        DeviceReadingsBetweenServers deviceReadingsBetweenServers = new DeviceReadingsBetweenServers();
        deviceReadingsBetweenServers.setDeviceDataList(createDeviceReadingsBetweenServer(deviceReadingsFromGateway.getDeviceDataList()));
        deviceReadingsBetweenServers.setGatewayTimestamp(deviceReadingsFromGateway.getGatewayTimestamp());
        GatewayDataForDpProtos.DevicesReadingsBetweenServers devicesReadingsBetweenServersProto = devicesReadingsBetweenServersSerializer.serialize(deviceReadingsBetweenServers);
        GatewaySensorReadingsServiceGrpc.GatewaySensorReadingsServiceBlockingStub stub = GatewaySensorReadingsServiceGrpc.newBlockingStub(managedChannel);
        CommnStructuresProtos.DeviceReadingsResponse response = stub.gatewaySensorReadings(devicesReadingsBetweenServersProto);
        s_logger.info("Response received from GRPC server is {}", response.getStatus());

        // De-serializing the response received from Data processing application
        DeviceReadingsResponse responseFromDPDeserialized = deviceReadingsResponseDeserializer.deserializeResponseBetweenServers(response);
        if (responseFromDPDeserialized.getResponseStatus() == ResponseStatus.FAILED) {
            //TODO: Keep a temporary storage of the readings, to be sent again
            responseFromDPDeserialized.setResponseStatus(ResponseStatus.SUCCESS); //Setting it to success for the gateway
        }
        if (gatewayDiscoveryRequired)
            responseFromDPDeserialized.setResponseStatus(ResponseStatus.SUCCESS_RESEND_DEVICE_DISCOVERY);

        // Send response back to the gateway
        s_logger.info("Sending response back to publisher on response topic {}. Response {}", responseTopic, responseFromDPDeserialized.getResponseStatus());
        CommnStructuresProtos.DeviceReadingsResponse responseToGatewayProto =
                responseToGatewaySerializer.serialize(responseFromDPDeserialized);
        try {
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(byteOutputStream);
            os.writeObject(responseToGatewayProto);
            MqttMessage messageToSend = new MqttMessage(byteOutputStream.toByteArray());
            MqttMessageWrapper mqttMessageWrapper = new MqttMessageWrapper();
            mqttMessageWrapper.setTopic(responseTopic);
            mqttMessageWrapper.setMqttMessage(messageToSend);
            subscriberResponse.schedule(mqttMessageWrapper);
        } catch (IOException e) {
        }
    }

    private void updateDevicesAttachedToGatewayInDatabase(List<DeviceData> deviceDataList) {
        s_logger.info("Checking if all devices/sensors are added to DB. Device list size {}", deviceDataList.size());

        List<String> deviceNames = new ArrayList<>();
        for (DeviceData deviceData : deviceDataList){
            deviceNames.add(deviceData.getDeviceName());
        }
        s_logger.trace("Get gateway from db for gateway name {} and customer {}", gatewayName, customerName);
        gateway = databaseHelper.getGatewayFromNameAndCustomerName(gatewayName, customerName);
        s_logger.trace("Gateway found. {}", gateway.toString());
        boolean newDeviceFound = databaseHelper.saveDevicesForGateway(gateway, deviceNames);
        gatewayDiscoveryRequired = newDeviceFound;
        if (gatewayDiscoveryRequired == true && gateway.isDiscoveryRequired() != true) {
            gateway.setDiscoveryRequired(gatewayDiscoveryRequired);
            databaseHelper.updateGateway(gateway);
        } else if (gateway.isDiscoveryRequired() == true) {
            gatewayDiscoveryRequired = true;
        }
    }

    private List<DeviceDataBetweenServers> createDeviceReadingsBetweenServer(List<DeviceData> deviceDataList) {
        List<DeviceDataBetweenServers> deviceDataBetweenServersList = new ArrayList<>();
        for (DeviceData deviceData : deviceDataList){
            Sensor sensor = databaseHelper.findSensorByNameAndGateway(deviceData.getDeviceName(), gateway);
            DeviceDataBetweenServers deviceDataBetweenServers = new DeviceDataBetweenServers();
            deviceDataBetweenServers.setDeviceId(sensor.getSensorId());
            String measurementUnit = "--";
            if (sensor.getType() == DeviceType.TEMPERATURE_SENSOR.name()) {
                measurementUnit = "degree";
            } else if (sensor.getType() == DeviceType.HUMIDITY_SENSOR.name()) {
                measurementUnit = "mg/L";
            } else if (sensor.getType() == DeviceType.MOTION_SENSOR.name()) {
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

    private void sendResponseToGateway(DeviceReadingsResponse responseFromDPDeserialized) {
        try {
            //TODO: Send a response back to the gateway device !!!
            DeviceReadingsResponse responseToGateway = new DeviceReadingsResponse();
            responseToGateway.setResponseStatus(responseFromDPDeserialized.getResponseStatus());
            s_logger.info("Sending response back to publisher on response topic {}. Response {}", responseTopic, responseToGateway.getResponseStatus());
            CommnStructuresProtos.DeviceReadingsResponse responseToGatewayProto =
                    responseToGatewaySerializer.serialize(responseToGateway);
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(byteOutputStream);
            os.writeObject(responseToGatewayProto);
            MqttMessage messageToSend = new MqttMessage(byteOutputStream.toByteArray());
            MqttMessageWrapper mqttMessageWrapper = new MqttMessageWrapper();
            mqttMessageWrapper.setMqttMessage(messageToSend);
            mqttMessageWrapper.setTopic(responseTopic);
            subscriberResponse.schedule(mqttMessageWrapper);
         //   mqttClient.publish(responseTopic, messageToSend);
            s_logger.info("Response sent on response topic {} successfully", responseTopic);
        }  catch (IOException e) {

        }
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}
