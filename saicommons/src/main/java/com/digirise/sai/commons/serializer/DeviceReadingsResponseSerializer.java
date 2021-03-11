package com.digirise.sai.commons.serializer;

import com.digirise.proto.CommonStructuresProto;
import com.digirise.sai.commons.helper.DeviceReadingsResponse;
import com.digirise.sai.commons.helper.ResponseStatus;
import org.springframework.stereotype.Component;

/**
 * DeviceReadingsResponseSerializer class is used to serialize the response to
 * 1. Gateway discovery message and data message response sent by the backend server to gateway.
 * 2. Most likely the gRPC message response sent by data processor server to backend server
 * Created by IntelliJ IDEA.
 * Date: 2020-05-15
 * Author: shrinkhlak
 */

@Component
public class DeviceReadingsResponseSerializer {
    public CommonStructuresProto.DeviceReadingsResponse serialize(DeviceReadingsResponse responseToGateway) {
        CommonStructuresProto.DeviceReadingsResponse.Builder responseToGatewayProto =
                CommonStructuresProto.DeviceReadingsResponse.newBuilder();
        if (responseToGateway.getResponseStatus() == ResponseStatus.SUCCESS)
            responseToGatewayProto.setStatus(CommonStructuresProto.ResponseStatus.SUCCESS);
        else if (responseToGateway.getResponseStatus() == ResponseStatus.SUCCESS_RESEND_DEVICE_DISCOVERY)
            responseToGatewayProto.setStatus(CommonStructuresProto.ResponseStatus.SUCCESS_RESEND_DEVICE_DISCOVERY);
        else if (responseToGateway.getResponseStatus() == ResponseStatus.FAILED)
            responseToGatewayProto.setStatus(CommonStructuresProto.ResponseStatus.FAILED);
        return responseToGatewayProto.build();
    }
}
