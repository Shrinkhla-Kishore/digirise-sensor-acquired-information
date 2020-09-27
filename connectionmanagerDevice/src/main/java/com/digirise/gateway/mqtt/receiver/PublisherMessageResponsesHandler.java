package com.digirise.gateway.mqtt.receiver;

import com.digirise.gateway.mqtt.sender.MessagePublisher;
import com.digirise.proto.GatewayDataProtos;
import com.digirise.sai.commons.readings.DeviceReadingsFromGateway;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-06-14
 * Author: shrinkhlak
 */

@Service
public class PublisherMessageResponsesHandler implements Runnable {
    private static final Logger s_logger = LoggerFactory.getLogger(PublisherMessageResponsesHandler.class);
    @Autowired
    private MessagePublisher messagePublisher;
    private static final long TIME_TO_WAIT_SECONDS = 120;
    private ExecutorService callbackExecutor;
    private Timestamp discoveryRespExpirationTs;
    private ConcurrentHashMap<UUID, DeviceReadingsFromGateway> deviceDataResponseMap;

    public PublisherMessageResponsesHandler() {
        deviceDataResponseMap = new ConcurrentHashMap<>();
        callbackExecutor = Executors.newSingleThreadScheduledExecutor();
        ((ScheduledExecutorService) callbackExecutor).scheduleWithFixedDelay(this, 10000, 5000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        try {
            s_logger.debug("Executing the run method");
            Timestamp currentTimestamp = new Timestamp(new Date().getTime());
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            //Check the discovery timestamp
            if (discoveryRespExpirationTs != null && discoveryRespExpirationTs.before(currentTimestamp)) {
                s_logger.warn("No response received now {} for the discovery message expired at {}.",
                        currentTimestamp, discoveryRespExpirationTs);
                messagePublisher.sendGatewayDiscoveryInfo();
            } else if (discoveryRespExpirationTs != null) {
                s_logger.debug("Discovery message not expired. Expiration ts {}, current ts {}", discoveryRespExpirationTs, currentTimestamp);
            }
            for (Map.Entry<UUID, DeviceReadingsFromGateway> entry : deviceDataResponseMap.entrySet()) {
                LocalDateTime localDateTime = entry.getValue().getGatewayTimestamp().toLocalDateTime().plusSeconds(TIME_TO_WAIT_SECONDS);
                Timestamp expirationTimestamp = Timestamp.valueOf(localDateTime);
                if (expirationTimestamp.before(currentTimestamp)) {
                    s_logger.warn("The request response is not received for UUID {}, re-trying", entry.getKey());
                    DeviceReadingsFromGateway deviceReadingsFromGateway = entry.getValue();
                    deviceReadingsFromGateway.setGatewayTimestamp(currentTimestamp);
                    GatewayDataProtos.DevicesReadingsFromGateway gatewayReadings = messagePublisher.serializeDeviceReadingsFromGateway(deviceReadingsFromGateway);
                    messagePublisher.publishInformation(gatewayReadings, messagePublisher.createAlarmTopic(entry.getKey()));
                    deviceDataResponseMap.put(entry.getKey(), deviceReadingsFromGateway);
                }

            }
            s_logger.debug("Returning now from the run method");
        }catch (IOException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void addResponseAwaited(UUID uuid, DeviceReadingsFromGateway deviceReadingsFromGateway){
        s_logger.debug("Current number of items in map {}", deviceDataResponseMap.size());
        deviceDataResponseMap.put(uuid, deviceReadingsFromGateway);
        s_logger.debug("Added UUID {} to map, total map size is {}", uuid, deviceDataResponseMap.size());
    }

    public void responseReceived(UUID uuid){
        deviceDataResponseMap.remove(uuid);
        s_logger.debug("Removing UUID {}. Remaining map size is {}", uuid, deviceDataResponseMap.size());
    }

    public void discoveryMessageResponseReceived(){
        s_logger.info("Discovery message response is received, setting discoveryRespExpirationTs to null");
        this.discoveryRespExpirationTs = null;
    }

    public void setDiscoveryRespExpirationTs(Timestamp discoveryRespExpirationTs) {
        if (discoveryRespExpirationTs != null) {
            LocalDateTime localDateTime = discoveryRespExpirationTs.toLocalDateTime().plusSeconds(TIME_TO_WAIT_SECONDS);
            this.discoveryRespExpirationTs = Timestamp.valueOf(localDateTime);
            s_logger.info("Set the discovery response time to {} plus {} seconds", discoveryRespExpirationTs.toLocalDateTime(), TIME_TO_WAIT_SECONDS);
        } else {
            discoveryRespExpirationTs = null;
        }
    }

}
