package com.digirise.server.grpc.clientside;

import com.digirise.proto.CommonStructuresProto;
import com.digirise.proto.GatewayDataForDpProto;
import com.digirise.proto.GatewayDataProto;
import com.digirise.proto.GatewaySensorReadingsServiceGrpc;
import com.digirise.sai.commons.deserializer.DeviceReadingsResponseDeserializer;
import com.digirise.sai.commons.helper.DeviceReadingsResponse;
import com.digirise.sai.commons.helper.ResponseStatus;
import com.digirise.sai.commons.readings.DeviceReadingsFromGateway;
import com.digirise.sai.commons.servercommunication.DeviceReadingsBetweenServers;
import com.digirise.server.model.Gateway;
import com.digirise.server.mqtt.receiver.deserialize.DeviceReadingsFromGatewayDeserializer;
import com.digirise.server.mqtt.receiver.serialize.DevicesReadingsBetweenServersSerializer;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** DataProcessingMessageHandler object is created for each data message received from gateway.
 * This is the main class that calls methods on {@link DataProcessingHandler} class to
 * update devices and gateway information in db and to create and dispatch request to DP server.
 *
 * Created by IntelliJ IDEA.
 * Date: 2020-05-14
 * Author: shrinkhlak
 */
public class DataProcessingMessageHandler implements Runnable{
    public static final Logger s_logger = LoggerFactory.getLogger(DataProcessingMessageHandler.class);
    private Gateway gateway;
    private ManagedChannel managedChannel;
    private String responseTopic;
    private DeviceReadingsFromGatewayDeserializer deviceReadingsFromGatewayDeserializer;
    private DevicesReadingsBetweenServersSerializer devicesReadingsBetweenServersSerializer;
    private DeviceReadingsResponseDeserializer deviceReadingsResponseDeserializer;
    private DataProcessingHandler dataProcessingHandler;
    private boolean gatewayDiscoveryRequired;
    private boolean resendRequest;
    private GatewayDataProto.DevicesReadingsFromGateway devicesReadingsFromGatewayProto;
    private DeviceReadingsFromGateway deviceReadingsFromGateway;


    public DataProcessingMessageHandler(ManagedChannel managedChannel, String topic, DataProcessingHandler dataProcessingHandler, DeviceReadingsFromGatewayDeserializer deviceReadingsFromGatewayDeserializer,
                                        DevicesReadingsBetweenServersSerializer devicesReadingsBetweenServersSerializer, DeviceReadingsResponseDeserializer deviceReadingsResponseDeserializer,
                                        GatewayDataProto.DevicesReadingsFromGateway devicesReadingsFromGatewayProto) {
        this.managedChannel = managedChannel;
        this.responseTopic = topic + "/response";
        this.dataProcessingHandler = dataProcessingHandler;
        this.deviceReadingsFromGatewayDeserializer = deviceReadingsFromGatewayDeserializer;
        this.devicesReadingsBetweenServersSerializer = devicesReadingsBetweenServersSerializer;
        this.deviceReadingsResponseDeserializer = deviceReadingsResponseDeserializer;
        this.devicesReadingsFromGatewayProto = devicesReadingsFromGatewayProto;
        gatewayDiscoveryRequired = false;
        resendRequest = false;
    }

    @Override
    public void run() {
        // Deserialize the device readings, Add the devices to the database, if not there already
        deserializeAndHandleDatabase();

        //Create new DeviceReadingsBetweenServer proto object and send to DP application
        sendDeviceReadingsToDP();
    }

    private void deserializeAndHandleDatabase() {
        // Deserialize the device readings
        deviceReadingsFromGateway = deviceReadingsFromGatewayDeserializer.deserializeDeviceReadingsFromGateway(devicesReadingsFromGatewayProto);
        String gatewayName = deviceReadingsFromGateway.getGatewayName();
        String customerName = deviceReadingsFromGateway.getCustomerName();

        // Add the devices to the database, if not there already
        if (deviceReadingsFromGateway.getDeviceDataList().size() > 0) {
            gateway = dataProcessingHandler.updateDevicesAttachedToGatewayInDatabase(deviceReadingsFromGateway.getDeviceDataList(), gatewayName, customerName);
            gatewayDiscoveryRequired = gateway.isDiscoveryRequired();
        }
    }

    public void sendDeviceReadingsToDP() {
        s_logger.trace("Sending device readings for response topic {} to DP", responseTopic);
        String gatewayName = deviceReadingsFromGateway.getGatewayName();

        //Create new DeviceReadingsBetweenServer proto object and send to DP application
        DeviceReadingsBetweenServers deviceReadingsBetweenServers = new DeviceReadingsBetweenServers();
        deviceReadingsBetweenServers.setDeviceDataList(dataProcessingHandler.createDeviceReadingsBetweenServer(deviceReadingsFromGateway.getDeviceDataList(), gateway));
        deviceReadingsBetweenServers.setGatewayTimestamp(deviceReadingsFromGateway.getGatewayTimestamp());
        GatewayDataForDpProto.DevicesReadingsBetweenServers devicesReadingsBetweenServersProto = devicesReadingsBetweenServersSerializer.serialize(deviceReadingsBetweenServers);
        GatewaySensorReadingsServiceGrpc.GatewaySensorReadingsServiceFutureStub stub = GatewaySensorReadingsServiceGrpc.newFutureStub(managedChannel);
        ListenableFuture<CommonStructuresProto.DeviceReadingsResponse> response = stub.gatewaySensorReadings(devicesReadingsBetweenServersProto);
        Futures.addCallback(response, new FutureCallback<CommonStructuresProto.DeviceReadingsResponse>() {
            @Override
            public void onSuccess(@NullableDecl CommonStructuresProto.DeviceReadingsResponse deviceReadingsResponse) {
                // De-serializing the response received from Data processing application
                DeviceReadingsResponse responseFromDPDeserialized = deviceReadingsResponseDeserializer.deserializeResponseBetweenServers(deviceReadingsResponse);
                if (responseFromDPDeserialized.getResponseStatus() == ResponseStatus.FAILED) {
                    s_logger.warn("failed to send message to data processor, store it for re-sending ");
                    handleResponse(responseFromDPDeserialized, false);
                    //   responseFromDPDeserialized.setResponseStatus(ResponseStatus.SUCCESS); //Setting it to success for the gateway
                } else {
                    s_logger.info("Message sent successfully to data processor");
                    handleResponse(responseFromDPDeserialized, true);
                }
            }
            @Override
            public void onFailure(Throwable throwable) {
                s_logger.warn("failed to send message to DP for gateway {}, store it for re-sending", gatewayName);
                handleResponse(null, false);
            }
        }, dataProcessingHandler.getDpExecutorService());
        s_logger.info("Message for gateway {} on response topic {} dispatched to DP",
                gatewayName, responseTopic);
    }

    private void handleResponse(DeviceReadingsResponse responseFromDPDeserialized, boolean successOutcome){
        if (successOutcome == false) {
            responseFromDPDeserialized = new DeviceReadingsResponse();
            responseFromDPDeserialized.setResponseStatus(ResponseStatus.FAILED);
        }
        if (gatewayDiscoveryRequired)
            responseFromDPDeserialized.setResponseStatus(ResponseStatus.SUCCESS_RESEND_DEVICE_DISCOVERY);

        // Send response back to the gateway
        if (resendRequest == false) {
            s_logger.info("Sending response back on response topic {}",
                    responseFromDPDeserialized.getResponseStatus(), responseTopic);
            dataProcessingHandler.sendResponseToGateway(responseFromDPDeserialized, responseTopic);
        }

        //Archive the message to be re-sent
        if (successOutcome == false) {
            resendRequest = true;
            dataProcessingHandler.addDpMessagesToResend(this);
        }
    }

    public String getResponseTopic() {
        return responseTopic;
    }
}
