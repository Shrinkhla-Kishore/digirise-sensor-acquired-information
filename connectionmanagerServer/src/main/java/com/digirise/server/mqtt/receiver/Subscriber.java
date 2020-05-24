package com.digirise.server.mqtt.receiver;

import com.digirise.proto.GatewayDataProtos;
import com.digirise.proto.GatewayDiscoveryProtos;
import com.digirise.sai.commons.discovery.GatewayDiscovery;
import com.digirise.sai.commons.helper.DeviceReadingsResponse;
import com.digirise.server.handler.DataProcessingGrpcClient;
import com.digirise.server.handler.DataProcessingMessageHandler;
import com.digirise.server.handler.DatabaseHelper;
import com.digirise.server.mqtt.receiver.deserialize.DeviceReadingsFromGatewayDeserializer;
import com.digirise.server.mqtt.receiver.deserialize.DeviceReadingsResponseDeserializer;
import com.digirise.server.mqtt.receiver.deserialize.GatewayDiscoveryDeserializer;
import com.digirise.server.mqtt.receiver.serialize.DeviceReadingsResponseSerializer;
import com.digirise.server.mqtt.receiver.serialize.DevicesReadingsBetweenServersSerializer;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
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
public class Subscriber /*implements Runnable */ {
    private static final Logger s_logger = LoggerFactory.getLogger(Subscriber.class);
    @Value("${mqtt.broker}")
    private String s_mqttBroker;
    @Value("${mqtt.data.topic}")
    private String dataTopic;
    @Value("${mqtt.info.topic}")
    private String infoTopic;
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
        deviceDataHandlerPool = new ThreadPoolExecutor(10, 10, 60000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }

    public void subscribeGatewayDiscoveryTopic() {
        try {
            if (mqttClient.isConnected()){
                mqttClient.subscribe(infoTopic, (topic, message) -> {
                    if (mqttClient.isConnected()) {
                        s_logger.info("Received message of size {} bytes on topic {}", message.getPayload().length, topic);
                        // Reading the client Id
                        int prefixLength = "gateway/".length();
                        String topicTemp = topic.substring(prefixLength);
                        int lastIndex = topicTemp.indexOf("/");
                        String gatewayId = topicTemp.substring(0, lastIndex);
                        s_logger.info("Received data from gateway mqttClientId {}, message Id: {}", gatewayId, message.getId());
                        if (!deviceConnectedClients.contains(gatewayId)) {
                            deviceConnectedClients.add(gatewayId);
                            s_logger.info("Added new client {} to connected Device list", gatewayId);
                        }
                        s_logger.info("GATEWAY DISCOVERY INFORMATION RECEIVED FROM GATEWAY {} with messageId:", gatewayId, message.getId());
                        ByteArrayInputStream bis = new ByteArrayInputStream(message.getPayload());
                        ObjectInput in = new ObjectInputStream(bis);
                        GatewayDiscoveryProtos.GatewayDiscovery gatewayDiscoveryProto = (GatewayDiscoveryProtos.GatewayDiscovery) in.readObject();

                        GatewayDiscovery gatewayDiscovery = gatewayDiscoveryDeserializer.deserializeGatewayDiscovery(gatewayDiscoveryProto);
                        s_logger.info("GatewayDiscovery message contains gatewayName {}, customerName {}, customerId {}, location {}, coordinates {}, timestamp {} ",
                                gatewayDiscovery.getGatewayName(), gatewayDiscovery.getCustomerName(), gatewayDiscovery.getCustomerId(),
                                gatewayDiscovery.getLocation(), gatewayDiscovery.getCoordinates(), gatewayDiscovery.getTimestamp());

                        // Save to database, check that customerName or Id is found. If, so then save the gateway information
                        databaseHelper.saveGatewayToDatabase(gatewayDiscovery);
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
                        s_logger.info("Received data from gateway mqttClientId/gatewayId {}, message Id: {}", gatewayId, message.getId());
                        if (!deviceConnectedClients.contains(gatewayId)) {
                            deviceConnectedClients.add(gatewayId);
                            s_logger.info("Added new client {} to connected Device list", gatewayId);
                        }
                        s_logger.info(" SENSOR DATA RECEIVED FROM GATEWAY {} with messageId:", gatewayId, message.getId());
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
//                        String responseTopic = topic + "/response";
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
