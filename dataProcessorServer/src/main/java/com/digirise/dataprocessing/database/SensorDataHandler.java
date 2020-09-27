package com.digirise.dataprocessing.database;

import com.digirise.dataprocessing.InfluxDbStarter;
import com.digirise.sai.commons.helper.DeviceReading;
import com.digirise.sai.commons.servercommunication.DeviceDataBetweenServers;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-11
 * Author: shrinkhlak
 */

@Service
public class SensorDataHandler {
    public static final Logger s_logger = LoggerFactory.getLogger(SensorDataHandler.class);
    @Autowired
    private InfluxDbStarter influxDbStarter;
    private InfluxDBMapper influxDBMapper;
    private InfluxDB influxDB;

    @PostConstruct
    public void setDatabaseParameters(){
        influxDB = influxDbStarter.getInfluxDB();
        influxDBMapper = new InfluxDBMapper(influxDB);
    }

    public void getSensorMeasurements() {
        List<SensorMeasurement> sensorMeasurements = influxDBMapper.query(SensorMeasurement.class);
        s_logger.info("Size of entries in sensor_measurements table is {}", sensorMeasurements.size());
        for (SensorMeasurement sensorMeasurement : sensorMeasurements) {
            s_logger.info("measurement is {}, {}, {}, {}, {}", sensorMeasurement.getSensorId(), sensorMeasurement.getSensorName(),
                    sensorMeasurement.getTime(), sensorMeasurement.getUnit(), sensorMeasurement.getValue());
        }
    }

    public boolean createSensorMeasurement(List<DeviceDataBetweenServers> deviceDataBetweenServersList, Timestamp gatewayTimestamp) {
        BatchPoints batchPoints = BatchPoints.database(influxDbStarter.getDatabase()).build();
      //  List<SensorMeasurement> sensorMeasurementList = new ArrayList<>();
        for (DeviceDataBetweenServers deviceDataBetweenServers : deviceDataBetweenServersList) {
            Timestamp timestampToUse = gatewayTimestamp;
            if (deviceDataBetweenServers.getTimestamp() != null)
                timestampToUse = deviceDataBetweenServers.getTimestamp();

//            String measurementUnit = "--";
//            if (deviceDataBetweenServers.getTimestamp() != null)
//                timestampToUse = deviceDataBetweenServers.getTimestamp();
//            if (deviceDataBetweenServers.getDeviceType() == GatewayDataProtos.DeviceType.TEMPERATURE_SENSOR) {
//                measurementUnit = "degree";
//            } else if (deviceDataBetweenServers.getDeviceType() == GatewayDataProtos.DeviceType.HUMIDITY_SENSOR) {
//                measurementUnit = "mg/L";
//            } else if (deviceDataBetweenServers.getDeviceType() == GatewayDataProtos.DeviceType.MOTION_SENSOR) {
//                measurementUnit = "cm"; //TODO: This needs to be checked and refined.
//            }
            for (DeviceReading deviceReading : deviceDataBetweenServers.getDeviceReadings()) {
                Point point = Point.measurement("sensor_measurements")
                        .time(timestampToUse.toInstant().toEpochMilli(), TimeUnit.MILLISECONDS)
                        .tag("sensor_id", String.valueOf(deviceDataBetweenServers.getDeviceId()))
                        .addField("value", deviceReading.getValue())
                        .addField("unit", deviceReading.getUnit())
                        .build();
                batchPoints.point(point);
//                SensorMeasurement sensorMeasurement = new SensorMeasurement();
//                sensorMeasurement.setSensorId(deviceDataBetweenServers.getDeviceId());
//                if (deviceDataBetweenServers.getTimestamp() != null)
//                    sensorMeasurement.setTime(deviceDataBetweenServers.getTimestamp().toInstant());
//                else
//                    sensorMeasurement.setTime(gatewayTimestamp.toInstant());
//                influxDBMapper.save(sensorMeasurement);
            }
        }
        influxDB.write(batchPoints);
        s_logger.info("Written {} points in DB", batchPoints.getPoints().size());
        return true;
    }
}
