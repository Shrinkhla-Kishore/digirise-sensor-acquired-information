package com.digirise.server.mqtt.receiver;

import com.digirise.proto.CommnStructuresProtos;
import com.digirise.proto.GatewayDataProtos;
import com.digirise.proto.GatewayDiscoveryProtos;
import com.digirise.sai.commons.discovery.GatewayDiscovery;
import com.digirise.sai.commons.helper.DeviceReadingsResponse;
import com.digirise.sai.commons.helper.ResponseStatus;
import com.digirise.server.handler.DataProcessingGrpcClient;
import com.digirise.server.handler.DataProcessingMessageHandler;
import com.digirise.server.handler.DatabaseHelper;
import com.digirise.server.handler.MqttMessageWrapper;
import com.digirise.server.mqtt.receiver.deserialize.DeviceReadingsFromGatewayDeserializer;
import com.digirise.server.mqtt.receiver.deserialize.DeviceReadingsResponseDeserializer;
import com.digirise.server.mqtt.receiver.deserialize.GatewayDiscoveryDeserializer;
import com.digirise.server.mqtt.receiver.serialize.DeviceReadingsResponseSerializer;
import com.digirise.server.mqtt.receiver.serialize.DevicesReadingsBetweenServersSerializer;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class Subscriber {
    private static final Logger s_logger = LoggerFactory.getLogger(Subscriber.class);
    @Value("${mqtt.broker}")
    private String s_mqttBroker;
    @Value("${mqtt.data.topic}")
    private String dataTopic;
    @Value("${mqtt.info.topic}")
    private String discoveryTopic;
    @Autowired
    private GatewayDiscoveryDeserializer gatewayDiscoveryDeserializer;
    @Autowired
    private DeviceReadingsFromGatewayDeserializer deviceReadingsFromGatewayDeserializer;
    @Autowired
    private DeviceReadingsResponseDeserializer deviceReadingsResponseDeserializer;
    @Autowired
    private DevicesReadingsBetweenServersSerializer devicesReadingsBetweenServersSerializer;
    @Autowired
    private DeviceReadingsResponseSerializer deviceReadingsResponseSerializer;
    @Autowired
    private DatabaseHelper databaseHelper;
    @Autowired
    private DataProcessingGrpcClient dataProcessingGrpcClient;
    @Autowired
    private SubscriberResponse subscriberResponse;
    private String mqttClientId;
    private MqttClient mqttClient;
    private List<String> deviceConnectedClients;
    private ThreadPoolExecutor deviceDataHandlerPool;

    public Subscriber() {
        deviceConnectedClients = new ArrayList<>();
        deviceDataHandlerPool = new ThreadPoolExecutor(1, 5, 60000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }

    public void subscribeGatewayDiscoveryTopic() {
        try {
            if (mqttClient.isConnected()){
                mqttClient.subscribe(discoveryTopic, (topic, message) -> {
                    if (mqttClient.isConnected()) {
                        String responseTopic = topic + "/response";
                        s_logger.debug("Received message of size {} bytes on topic {}", message.getPayload().length, topic);
                        // Reading the client Id
                        int prefixLength = "gateway/".length();
                        String topicTemp = topic.substring(prefixLength);
                        int lastIndex = topicTemp.indexOf("/");
                        String gatewayId = topicTemp.substring(0, lastIndex);
                        s_logger.debug("GATEWAY DISCOVERY INFORMATION RECEIVED FROM GATEWAY {}, message Id: {}", gatewayId, message.getId());
                        if (!deviceConnectedClients.contains(gatewayId)) {
                            deviceConnectedClients.add(gatewayId);
                            s_logger.trace("Added new client {} to connected Device list", gatewayId);
                        }
                        ByteArrayInputStream bis = new ByteArrayInputStream(message.getPayload());
                        ObjectInput in = new ObjectInputStream(bis);
                        GatewayDiscoveryProtos.GatewayDiscovery gatewayDiscoveryProto = (GatewayDiscoveryProtos.GatewayDiscovery) in.readObject();

                        GatewayDiscovery gatewayDiscovery = gatewayDiscoveryDeserializer.deserializeGatewayDiscovery(gatewayDiscoveryProto);
                        s_logger.info("GatewayDiscovery message contains gatewayName {}, customerName {}, customerId {}, location {}, coordinates {}, timestamp {} ",
                                gatewayDiscovery.getGatewayName(), gatewayDiscovery.getCustomerName(), gatewayDiscovery.getCustomerId(),
                                gatewayDiscovery.getLocation(), gatewayDiscovery.getCoordinates(), gatewayDiscovery.getTimestamp());

                        // Save to database, check that customerName or Id is found. If, so then save the gateway information
                        boolean result = databaseHelper.saveGatewayToDatabase(gatewayDiscovery);
                        DeviceReadingsResponse response = new DeviceReadingsResponse();
                        if (result == true)
                            response.setResponseStatus(ResponseStatus.SUCCESS);
                        else
                            response.setResponseStatus(ResponseStatus.FAILED);

                        CommnStructuresProtos.DeviceReadingsResponse readingResponseProto =
                                deviceReadingsResponseSerializer.serialize(response);
                        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
                        ObjectOutputStream os = new ObjectOutputStream(byteOutputStream);
                        os.writeObject(readingResponseProto);
                        MqttMessage messageToSend = new MqttMessage(byteOutputStream.toByteArray());
                        MqttMessageWrapper mqttMessageWrapper = new MqttMessageWrapper();
                        mqttMessageWrapper.setTopic(responseTopic);
                        mqttMessageWrapper.setMqttMessage(messageToSend);
                        s_logger.trace("Sending response back for gateway discovery on topic {} with response {}",
                                responseTopic, readingResponseProto.getStatus());
                        subscriberResponse.schedule(mqttMessageWrapper);
                    }
                });
            }
        }catch (Exception e) {

        }
    }

    public void subscribeDeviceDataTopic() {
        try {
            if (mqttClient.isConnected()){
                mqttClient.subscribe(dataTopic, (topic, message) -> {
                    if (mqttClient.isConnected()) {
                        s_logger.info("Received message of size {} bytes on topic {}", message.getPayload().length, topic);
                        // Reading the client Id
                        int prefixLength = "gateway/".length();
                        String topicTemp = topic.substring(prefixLength);
                        int lastIndex = topicTemp.indexOf("/");
                        String gatewayId = topicTemp.substring(0, lastIndex);
                        s_logger.info("SENSOR DATA RECEIVED FROM GATEWAY {}, message Id: {}", gatewayId, message.getId());
                        if (!deviceConnectedClients.contains(gatewayId)) {
                            deviceConnectedClients.add(gatewayId);
                            s_logger.trace("Added new client {} to connected Device list", gatewayId);
                        }
                        ByteArrayInputStream bis = new ByteArrayInputStream(message.getPayload());
                        ObjectInput in = new ObjectInputStream(bis);
                        GatewayDataProtos.DevicesReadingsFromGateway gatewayReadingsProtobuf = (GatewayDataProtos.DevicesReadingsFromGateway) in.readObject();

                        // Sending gateway readings to Data Processing application
                        DataProcessingMessageHandler dataProcessingMessageHandler = new DataProcessingMessageHandler(dataProcessingGrpcClient.getManagedChannel(), mqttClient,
                                topic, databaseHelper, deviceReadingsFromGatewayDeserializer, devicesReadingsBetweenServersSerializer, deviceReadingsResponseDeserializer,
                                deviceReadingsResponseSerializer, subscriberResponse, gatewayReadingsProtobuf);
                        deviceDataHandlerPool.execute(dataProcessingMessageHandler);
                   //     dataProcessingMessageHandler.handleMessage(gatewayReadingsProtobuf);
                        s_logger.info("GatewayReadings disptached for furthur handling");

                        //TODO: Send a response back to the gateway device !!!
                        String responseTopic = topic + "/response";
                        sendResponseToGateway(true, responseTopic);
//
//                        DeviceReadingsResponseToGateway responseToGateway = new DeviceReadingsResponseToGateway();
//                        responseToGateway.setResponseStatus(responseFromDPDeserialized.getResponseStatus());
//                        s_logger.info("Sending response back to publisher on response topic {}. Response {}", responseTopic, responseToGateway.getResponseStatus());
//                        GatewayDataProtos.DeviceReadingsResponseToGateway responseToGatewayProto =
//                                deviceReadingsResponseSerializer.serialize(responseToGateway);
//                        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
//                        ObjectOutputStream os = new ObjectOutputStream(byteOutputStream);
//                        os.writeObject(responseToGatewayProto);
//                        MqttMessage messageToSend = new MqttMessage(byteOutputStream.toByteArray());

//                        mqttClient.publish(responseTopic, messageToSend);
//                        s_logger.info("Response sent on response topic {} successfully", responseTopic);
                    } else {
                        s_logger.warn("Server application lost connection to broker {}", s_mqttBroker);
                //        configureMqtt();
                    }
                });
            } else {
                s_logger.warn("Disconnected to the mqtt broker {}", s_mqttBroker);
            //    configureMqtt();
            }
        } catch(MqttException e) {
        }
    }

    private void sendResponseToGateway(boolean responseResult, String responseTopic) throws IOException {
        s_logger.info("Sending response back to the gateway on response topic {}", responseTopic);
        DeviceReadingsResponse response = new DeviceReadingsResponse();
        if (responseResult == true)
            response.setResponseStatus(ResponseStatus.SUCCESS);
        else
            response.setResponseStatus(ResponseStatus.FAILED);

        CommnStructuresProtos.DeviceReadingsResponse readingResponseProto =
                deviceReadingsResponseSerializer.serialize(response);
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(byteOutputStream);
        os.writeObject(readingResponseProto);
        MqttMessage messageToSend = new MqttMessage(byteOutputStream.toByteArray());
        MqttMessageWrapper mqttMessageWrapper = new MqttMessageWrapper();
        mqttMessageWrapper.setTopic(responseTopic);
        mqttMessageWrapper.setMqttMessage(messageToSend);
        s_logger.trace("Sending response back for gateway discovery on topic {} with response {}",
                responseTopic, readingResponseProto.getStatus());
        subscriberResponse.schedule(mqttMessageWrapper);
    }


    private  void subscribeToData() throws MqttException {
        while (true){

        }
    }

    public void setMqttClientId(String mqttClientId) {
        this.mqttClientId = mqttClientId;
    }

    public void setMqttClient(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    public List<String> getDeviceConnectedClients() {
        return deviceConnectedClients;
    }

    public void cleanDeviceConnectedClients() {
        deviceConnectedClients.clear();
    }

    public void removeClient(String clientId){
        deviceConnectedClients.remove(clientId);
    }
}
