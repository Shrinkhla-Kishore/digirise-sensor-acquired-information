package com.digirise.gateway.mqtt.receiver.deserialization;

import com.digirise.proto.CommnStructuresProtos;
import com.digirise.sai.commons.helper.DeviceReadingsResponse;
import com.digirise.sai.commons.helper.ResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-13
 * Author: shrinkhlak
 */

@Component
public class DeviceReadingsResponseDeserializer {
    public static final Logger s_logger = LoggerFactory.getLogger(DeviceReadingsResponseDeserializer.class);

    public DeviceReadingsResponse deserializeResponseBetweenServers(
            CommnStructuresProtos.DeviceReadingsResponse responseProto) {
        s_logger.debug("Deserializing the response received from server instance");
        DeviceReadingsResponse response = new DeviceReadingsResponse();
        if (responseProto.getStatus() == CommnStructuresProtos.ResponseStatus.SUCCESS) {
            response.setResponseStatus(ResponseStatus.SUCCESS);
        } else if (responseProto.getStatus() == CommnStructuresProtos.ResponseStatus.SUCCESS_RESEND_DEVICE_DISCOVERY) {
            response.setResponseStatus(ResponseStatus.SUCCESS_RESEND_DEVICE_DISCOVERY);
        } else if (responseProto.getStatus() == CommnStructuresProtos.ResponseStatus.FAILED) {
            response.setResponseStatus(ResponseStatus.FAILED);
        }
        return response;
    }
}
