package com.digirise.server.handler;

import com.digirise.sai.commons.discovery.GatewayDiscovery;
import com.digirise.server.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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

    public void saveGatewayToDatabase(GatewayDiscovery gatewayDiscovery) {
        Optional<Customer> customerFromDB = null;
        if (gatewayDiscovery.getCustomerId() > 0) {
            customerFromDB = customerRepository.findById(gatewayDiscovery.getCustomerId());
        } else if (gatewayDiscovery.getCustomerName() != null) {
            customerFromDB = customerRepository.findCustomersByName(gatewayDiscovery.getCustomerName()).findFirst();
        }
        if (customerFromDB.isPresent()) {
            Customer customer = customerFromDB.get();
            s_logger.info("gateway discovery message: Valid customer {} found for gateway {}", customer.getName(), gatewayDiscovery.getGatewayName());
            Gateway gatewayToSave = new Gateway();
            gatewayToSave.setCustomer(customer);
            gatewayToSave.setName(gatewayDiscovery.getGatewayName());
            gatewayToSave.setLocation(gatewayDiscovery.getLocation());
            gatewayToSave.setCoordinates(gatewayDiscovery.getCoordinates());
            gatewayToSave.setDiscoveryRequired(false);
            s_logger.info("Saving gateway {} to database", gatewayToSave.getName());
            gatewayRepository.save(gatewayToSave);
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
