package com.digirise.sai.commons.deserializer;

import com.digirise.proto.CommonStructuresProto;
import com.digirise.sai.commons.helper.DeviceReadingsResponse;
import com.digirise.sai.commons.helper.ResponseStatus;
import org.springframework.stereotype.Component;

/**
 * DeviceReadingsResponseDeserializer class de-serializes the response object received. This class is used to de-serialize:
 * 1. Data message response sent by data processor server to the backend server.
 * 2. Data message response sent by backend server to gateway
 * 3. Gateway discovery message response sent by backend server to gateway
 *
 * Created by IntelliJ IDEA.
 * Date: 2020-05-13
 * Author: shrinkhlak
 */

@Component
public class DeviceReadingsResponseDeserializer {

    public DeviceReadingsResponse deserializeResponseBetweenServers(
            CommonStructuresProto.DeviceReadingsResponse responseProto) {
        DeviceReadingsResponse response = new DeviceReadingsResponse();
        if (responseProto.getStatus() == CommonStructuresProto.ResponseStatus.SUCCESS) {
            response.setResponseStatus(ResponseStatus.SUCCESS);
        } else if (responseProto.getStatus() == CommonStructuresProto.ResponseStatus.SUCCESS_RESEND_DEVICE_DISCOVERY) {
            response.setResponseStatus(ResponseStatus.SUCCESS_RESEND_DEVICE_DISCOVERY);
        } else if (responseProto.getStatus() == CommonStructuresProto.ResponseStatus.FAILED) {
            response.setResponseStatus(ResponseStatus.FAILED);
        }
        return response;
    }
}
