package com.digirise.server.mqtt.receiver.serialize;

import com.digirise.proto.CommnStructuresProtos;
import com.digirise.proto.GatewayDataProtos;
import com.digirise.sai.commons.helper.DeviceReadingsResponse;
import com.digirise.sai.commons.helper.ResponseStatus;
 import org.springframework.stereotype.Component;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-15
 * Author: shrinkhlak
 */

@Component
public class DeviceReadingsResponseSerializer {
    public CommnStructuresProtos.DeviceReadingsResponse serialize(DeviceReadingsResponse responseToGateway) {
        CommnStructuresProtos.DeviceReadingsResponse.Builder responseToGatewayProto =
                CommnStructuresProtos.DeviceReadingsResponse.newBuilder();
        if (responseToGateway.getResponseStatus() == ResponseStatus.SUCCESS)
            responseToGatewayProto.setStatus(CommnStructuresProtos.ResponseStatus.SUCCESS);
        else if (responseToGateway.getResponseStatus() == ResponseStatus.SUCCESS_RESEND_DEVICE_DISCOVERY)
            responseToGatewayProto.setStatus(CommnStructuresProtos.ResponseStatus.SUCCESS_RESEND_DEVICE_DISCOVERY);
        else if (responseToGateway.getResponseStatus() == ResponseStatus.FAILED)
            responseToGatewayProto.setStatus(CommnStructuresProtos.ResponseStatus.FAILED);
        return responseToGatewayProto.build();
    }
}
