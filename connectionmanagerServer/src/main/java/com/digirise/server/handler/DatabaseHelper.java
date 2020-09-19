package com.digirise.server.handler;

import com.digirise.sai.commons.discovery.GatewayDiscovery;
import com.digirise.server.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-13
 * Author: shrinkhlak
 */

@Service
public class DatabaseHelper {
    public static final Logger s_logger = LoggerFactory.getLogger(DatabaseHelper.class);
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private GatewayRepository gatewayRepository;
    @Autowired
    private SensorRepository sensorRepository;

    @Transactional
    public boolean saveGatewayToDatabase(GatewayDiscovery gatewayDiscovery){
        String customerName = gatewayDiscovery.getCustomerName();
        s_logger.info("Trying to find an existing customer '{}' in database", customerName);
        Optional<Customer> customerFromDB = null;
        if (customerName != null) {
            s_logger.info("Finding customer by name {}", customerName);
            Stream<Customer> customerStream = customerRepository.findCustomersByName(customerName);
            s_logger.info("Found customer for customer name {}", customerName);
            customerFromDB = customerStream.findFirst();
        } else if (gatewayDiscovery.getCustomerId() > 0) {
            customerFromDB = customerRepository.findById(gatewayDiscovery.getCustomerId());
        }
        if (customerFromDB.isPresent()) {
            s_logger.info("customer object found in DB for customer name {}", customerName);
            Customer customer = customerFromDB.get();
            s_logger.info("gateway discovery message: Valid customer {} found for gateway {}", customer.getName(), gatewayDiscovery.getGatewayName());
            Optional<Gateway> gatewayFromDb = gatewayRepository.findGatewayByName(gatewayDiscovery.getGatewayName(), customerFromDB.get()).findFirst();
            if (gatewayFromDb.isPresent()) {
                s_logger.info("Gateway already exists in database. Gateway {}", gatewayFromDb.get().toString());
                boolean updateGateway = false;
                Timestamp timestamp = new Timestamp(new Date().getTime());
                if (!gatewayFromDb.get().getCoordinates().equalsIgnoreCase(gatewayDiscovery.getCoordinates())) {
                    gatewayFromDb.get().setCoordinates(gatewayDiscovery.getCoordinates());
                    updateGateway = true;
                } else if (!gatewayFromDb.get().getLocation().equalsIgnoreCase(gatewayDiscovery.getLocation())) {
                    gatewayFromDb.get().setLocation(gatewayDiscovery.getLocation());
                    updateGateway = true;
                }
                if (updateGateway) {
                    gatewayFromDb.get().setLastUpdatedOn(timestamp);
                }
                gatewayFromDb.get().setLastConnected(timestamp);
                gatewayFromDb.get().setDiscoveryRequired(false);
                gatewayRepository.save(gatewayFromDb.get());
                //TODO: compare the devices and update devices if necessary.
            } else {
                s_logger.info("New gateway {} for customer {}, saving to database", gatewayDiscovery.getGatewayName(), customerFromDB.get().getName());
                Gateway gatewayToSave = new Gateway();
                Timestamp timestamp = new Timestamp(new Date().getTime());
                gatewayToSave.setCustomer(customer);
                gatewayToSave.setName(gatewayDiscovery.getGatewayName());
                gatewayToSave.setLocation(gatewayDiscovery.getLocation());
                gatewayToSave.setCoordinates(gatewayDiscovery.getCoordinates());
                gatewayToSave.setCreatedOn(timestamp);
                gatewayToSave.setLastConnected(timestamp);
                gatewayToSave.setDiscoveryRequired(false);
                s_logger.info("Saving gateway {} to database", gatewayToSave.getName());
                gatewayRepository.save(gatewayToSave);
            }
            return true;
        } else {
            s_logger.error("Customer {}:{} not found for gateway {}", gatewayDiscovery.getCustomerId(),
                    gatewayDiscovery.getCustomerName(), gatewayDiscovery.getGatewayName());
            return false;
        }
    }

    @Transactional
    public boolean saveDevicesForGateway(Gateway gateway, List<String> deviceNames){
        boolean gatewayDiscoveryReqd = false;
//        if (gateway != null){
            for (String deviceName : deviceNames){
                Optional<Sensor> sensorStream = sensorRepository.findSensorByNameAndGateway(deviceName, gateway).findFirst();
                if (!sensorStream.isPresent()) {
                    s_logger.info("Sensor not found with name {} and gateway Id {}", deviceName, gateway.getGatewayId());
                    Sensor sensor = new Sensor();
                    sensor.setGateway(gateway);
                    sensor.setSensorName(deviceName);
                    sensorRepository.save(sensor);
                    gatewayDiscoveryReqd = true;
                } else {
                    s_logger.info("Sensor with name {} for Gatway Id {} already exists in DB", deviceName, gateway.getGatewayId());
                }
            }
//        } else {
//            s_logger.warn("Devices cannot be stored in DB a no gateway found with name {} and customer name {}",
//                    gatewayName, customerName);
//            gatewayDiscoveryReqd = true;
//        }
        return gatewayDiscoveryReqd;
    }

    @Transactional
    public Gateway getGatewayFromNameAndCustomerName(String gatewayName, String customerName) {
        Stream<Customer> customerStream = customerRepository.findCustomersByName(customerName);
        Optional<Customer> customer = customerStream.findFirst();
        if (customer.isPresent()) {
            s_logger.info("Customer entity found for customer name {}", customerName);
            Optional<Gateway> gatewayStream = gatewayRepository.findGatewayByName(gatewayName, customer.get()).findFirst();
            if (gatewayStream.isPresent()) {
                s_logger.info("Gateway entity found for gateway {}", gatewayName);
                return gatewayStream.get();
            } else
                s_logger.warn("No gateway found with name {} for customer id {}", gatewayName, customer.get().getCustomerId());
                return null;
        }
        s_logger.warn("No customer found with the name {}", customerName);
        return null;
    }

    @Transactional
    public Sensor findSensorByNameAndGateway(String deviceName, Gateway gateway){
        Optional<Sensor> sensorStream = sensorRepository.findSensorByNameAndGateway(deviceName, gateway).findFirst();
        if (sensorStream.isPresent())
            return sensorStream.get();
        else
            return null;
    }

    public void updateGateway(Gateway gateway) {
        gatewayRepository.save(gateway);
    }

}
