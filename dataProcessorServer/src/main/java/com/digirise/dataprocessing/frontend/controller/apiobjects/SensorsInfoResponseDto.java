package com.digirise.dataprocessing.frontend.controller.apiobjects;

import com.digirise.sai.commons.helper.DeviceType;

import java.util.List;

/**
 * SensorsInfoResponseDto is the response object that contains the sensor info on the url /sensorInfo
 *
 * Created by IntelliJ IDEA.
 * Date: 2021-01-12
 * Author: shrinkhlak
 */
public class SensorsInfoResponseDto {
    private List<SensorInfo> sensorInfoList;

    public List<SensorInfo> getSensorInfoList() {
        return sensorInfoList;
    }

    public void setSensorInfoList(List<SensorInfo> sensorInfoList) {
        this.sensorInfoList = sensorInfoList;
    }
}
