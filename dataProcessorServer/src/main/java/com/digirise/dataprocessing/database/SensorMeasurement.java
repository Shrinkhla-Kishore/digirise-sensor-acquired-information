package com.digirise.dataprocessing.database;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.time.Instant;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-11
 * Author: shrinkhlak
 */

@Measurement(name = "sensor_measurements", database = "sensor_data")
public class SensorMeasurement {
    @Column(name = "time")
    private Instant time;

    @Column(name = "sensor_id")
    private String sensorId;

    @Column(name = "sensor_name")
    private String sensorName;

    @Column(name = "value")
    private String value;

    @Column(name = "unit")
    private String unit;

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
