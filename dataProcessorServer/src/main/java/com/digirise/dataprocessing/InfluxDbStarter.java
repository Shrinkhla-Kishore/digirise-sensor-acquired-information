package com.digirise.dataprocessing;

import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Pong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-11
 * Author: shrinkhlak
 */

@Component
public class InfluxDbStarter {
    public static final Logger s_logger = LoggerFactory.getLogger(InfluxDbStarter.class);

    private InfluxDB influxDB;
    @Value("${influxdb.url}")
    private String url;
    @Value("${influxdb.database}")
    private String database;
    @Value("${influxdb.loglevel}")
    private String logLevel;
    private String retentionPolicy;
//    @Value("${spring.influxdb.username}")
//    private String username;
//    @Value("${spring.influxdb.password}")
//    private String password;

    @PostConstruct
    public void connectInfluxDb(){
        try {
            while (true) {
                influxDB = InfluxDBFactory.connect(url);
                Pong pingResponse = influxDB.ping();
                if (pingResponse.getVersion().equalsIgnoreCase("unknown")) {
                    s_logger.error("Error pinging to Influx db server url {}.", url);
                    s_logger.error("Connection to database {} failed", database);
                    Thread.currentThread().sleep(5000);
                }
                else {
                    s_logger.info("Connected to influxDB url {} successfully", url);
                    influxDB.createDatabase(database);
                    influxDB.setDatabase(database);
                    retentionPolicy = "default";
                    influxDB.createRetentionPolicy(retentionPolicy, database, "300d", "30m", 1, true);
                    influxDB.setRetentionPolicy(retentionPolicy);
                    BatchOptions batchOptions = BatchOptions.DEFAULTS.actions(100).flushDuration(5000).precision(TimeUnit.MILLISECONDS);
                    influxDB.enableBatch(batchOptions);
//                    case(logLevel) {
//                    case "BASIC": influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);
//                    break;
//                    case "FULL": influxDB.setLogLevel(InfluxDB.LogLevel.FULL);
//                    break;
//                    case "HEADERS"
//                    }
                    if (logLevel.equalsIgnoreCase("BASIC"))
                        influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);
                    else if (logLevel.equalsIgnoreCase("FULL"))
                        influxDB.setLogLevel(InfluxDB.LogLevel.FULL);
                    else if (logLevel.equalsIgnoreCase("HEADERS"))
                        influxDB.setLogLevel(InfluxDB.LogLevel.HEADERS);
                    else
                        influxDB.setLogLevel(InfluxDB.LogLevel.NONE);
                    return;
                }
            }
        } catch (InterruptedException e) {
            s_logger.warn("Thread setting up connection to influxdb database {} interrupted", url);
        }
    }

    @PreDestroy
    public void closeInfluxDbConnection(){
        influxDB.disableBatch();
        influxDB.close();
        s_logger.info("Influx DB connection closed successfully");
    }

    public InfluxDB getInfluxDB() {
        return influxDB;
    }

    public String getUrl() {
        return url;
    }

    public String getDatabase() {
        return database;
    }

}
