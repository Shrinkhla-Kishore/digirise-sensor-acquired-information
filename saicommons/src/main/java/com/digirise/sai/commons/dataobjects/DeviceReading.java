package com.digirise.sai.commons.dataobjects;

import com.digirise.api.GatewayDataProtos;

public class DeviceReading {
    private GatewayDataProtos.ReadingType readingType;
    private String value;

    public GatewayDataProtos.ReadingType getReadingType() {
        return readingType;
    }

    public void setReadingType(GatewayDataProtos.ReadingType readingType) {
        this.readingType = readingType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /*    private enum ReadingType{
        SENSOR_CURRENT_VALUE,
        SENSOR_OTHER_VALUE;
    }*/
}
