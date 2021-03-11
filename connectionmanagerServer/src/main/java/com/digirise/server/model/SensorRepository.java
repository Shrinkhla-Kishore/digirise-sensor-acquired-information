package com.digirise.server.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface SensorRepository extends CrudRepository<Sensor, Long> {
    @Query("select ss from Sensor ss where ss.sensorName = :name and ss.gateway = :gateway")
    public Stream<Sensor> findSensorByNameAndGateway(@Param("name") String name, @Param("gateway") Gateway gatewayId);

    @Query("select ss from Sensor ss where ss.gateway = :gateway")
    public Stream<Sensor> findAllSensorByGatewayId(@Param("gateway") Gateway gateway);
}
