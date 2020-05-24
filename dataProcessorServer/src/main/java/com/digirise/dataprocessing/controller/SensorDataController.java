package com.digirise.dataprocessing.controller;

import com.digirise.dataprocessing.database.SensorDataHandler;
import com.digirise.proto.GatewaySensorReadingsServiceGrpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-11
 * Author: shrinkhlak
 */
@Component
public class SensorDataController extends GatewaySensorReadingsServiceGrpc.GatewaySensorReadingsServiceImplBase {
    @Autowired
    private SensorDataHandler sensorDataHandler;


}
