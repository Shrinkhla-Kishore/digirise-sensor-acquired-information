package com.digirise.gateway.mqtt.sender;

import com.digirise.gateway.ApplicationContextHandler;
import com.digirise.gateway.mqtt.receiver.DataMessageCallback;
import com.digirise.gateway.mqtt.receiver.GatewayDiscoveryMessageCallback;
import com.digirise.gateway.mqtt.receiver.PublisherMessageResponsesHandler;
import com.digirise.gateway.mqtt.sender.serialization.DevicesReadingsFromGatewaySerializer;
import com.digirise.gateway.mqtt.sender.serialization.GatewayDiscoverySerializer;
import com.digirise.proto.GatewayDataProto;
import com.digirise.proto.GatewayDiscoveryProto;
import com.digirise.sai.commons.deserializer.DeviceReadingsResponseDeserializer;
import com.digirise.sai.commons.discovery.GatewayDiscovery;
import com.digirise.sai.commons.helper.DeviceReading;
import com.digirise.sai.commons.helper.ReadingType;
import com.digirise.sai.commons.readings.DeviceData;
import com.digirise.sai.commons.readings.DeviceReadingsFromGateway;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MessagePublisher sets up the connection to towards the received mqtt broker url.
 * This class also handles creating and dispatching the request message for the
 * gateway discovery message and the gateway data message.
 * It also sets up to subscribe to the response messages
 * Created by IntelliJ IDEA.
 * Date: 2019-02-24
 * Author: shrinkhlak
 */

@Component
public class MessagePublisher {
    private static final Logger s_logger = LoggerFactory.getLogger(MessagePublisher.class);
    @Autowired
    private DevicesReadingsFromGatewaySerializer devicesReadingsFromGatewaySerializer;
    @Autowired
    private GatewayDiscoverySerializer gatewayDiscoverySerializer;
    @Autowired
    private PublisherMessageResponsesHandler publisherCallbackFactory;
    @Value("${gateway.name}")
    private String gatewayName;
    @Value("${gateway.customer.name}")
    private String gatewayCustomerName;
    @Value("${gateway.customer.id}")
    private String gatewayCustomerId;
    @Value("${gateway.location}")
    private String gatewayLocation;
    @Value("${gateway.coordinates}")
    private String gatewayCoordinates;
    private static final String SUFFIX_DATA_TOPIC = "/data/";
    private static final String SUFFIX_INFO_TOPIC = "/info/";
    private static final String PREFIX_TOPIC = "gateway/";
//    @Value("${ca.certificate}")
//    private Resource caCert;
//    @Value("${client.certificate}")
//    private Resource clientCert;
//    @Value("${client.key}")
//    private Resource clientKey;
    private AtomicInteger alarmId;
    public final int qosLevel = 2;
    private MqttClient mqttClient = null;
    private MqttConnectOptions options;

    public MessagePublisher() {
        alarmId = new AtomicInteger(40);
    }

    public synchronized void startMqttPublisher(String mqttBroker) {
            s_logger.info("creating publisher with gateway name {}", gatewayName);
            while (mqttClient == null || !mqttClient.isConnected()){
                try {
                    mqttClient = new MqttClient(mqttBroker, gatewayName);
                    setConnectOptions();
                    s_logger.info("Inside step 1");
                    Thread.currentThread().sleep(5000);
                    mqttClient.connect(options);
                    s_logger.info("Mqtt client connected to {}", mqttBroker);
                } catch (MqttException e) {
                    s_logger.warn("Error connecting to mqtt broker :( {}", mqttBroker);
                    s_logger.trace("{}", e.getStackTrace());
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (mqttClient.isConnected()) {
                s_logger.info("MessagePublisher connected to MQTT broker {}", mqttBroker);
//                String gatewayInfoTopicResp = PREFIX_TOPIC + gatewayName + SUFFIX_INFO_TOPIC + "+/response";
//              //  mqttClient.subscribe(gatewayInfoTopicResp, new ResponseCallback());
//                s_logger.info("MessagePublisher subscribed to topic {}", gatewayInfoTopicResp);
//                String topicName = "gateway/" + gatewayName + "/data/+/response";
//               // mqttClient.subscribe(topicName, new ResponseCallback());
//                // mqttClient.setCallback(new ResponseCallback());
//                s_logger.info("MessagePublisher subscribed to topic {}", topicName);
            }
    }

    public void sendData(String mqttBroker) throws MqttException, IOException {
        s_logger.info("Inside sendData");
        if (mqttClient.isConnected()) {
            if (checkIfDataToSend()) {
                UUID uuid = UUID.randomUUID();
                String alarm_topic = PREFIX_TOPIC + gatewayName + SUFFIX_DATA_TOPIC + uuid;

                List<DeviceData> fakeDevicesData = new ArrayList<>();
                //TO-DO: Handle real sensor data
                fakeDevicesData.add(createDeviceData());
                DeviceReadingsFromGateway deviceReadingsFromGateway = new DeviceReadingsFromGateway();
                deviceReadingsFromGateway.setCustomerName(gatewayCustomerName);
                deviceReadingsFromGateway.setGatewayName(gatewayName);
                deviceReadingsFromGateway.setGatewayTimestamp(new Timestamp(new Date().getTime()));
                deviceReadingsFromGateway.setDeviceDataList(fakeDevicesData);
                GatewayDataProto.DevicesReadingsFromGateway gatewayReadings = devicesReadingsFromGatewaySerializer.serializeDevicesData(deviceReadingsFromGateway);
                publishInformation(gatewayReadings,alarm_topic);

                DataMessageCallback callback = new DataMessageCallback((DeviceReadingsResponseDeserializer) ApplicationContextHandler.getBean(DeviceReadingsResponseDeserializer.class));
                callback.setUuid(uuid);
                publisherCallbackFactory.addResponseAwaited(uuid, deviceReadingsFromGateway);
                subscribeResponse(alarm_topic, callback);
            } else {
                s_logger.debug("No data to send from the connected devices");
            }
        } else {
            s_logger.warn("Mqtt gateway with Id {} not connected", gatewayName);
            startMqttPublisher(mqttBroker);
        }
    }

    public void sendGatewayDiscoveryInfo() throws IOException, MqttException {
        GatewayDiscovery gatewayDiscovery = new GatewayDiscovery();
        s_logger.info("Sending gatewayDiscovery information");
        s_logger.info("containing: gatewayName {}, customerName {}, CustomerId {}, location {}, coordinates {}",
                gatewayName, gatewayCustomerName, gatewayCustomerId, gatewayLocation, gatewayCoordinates);
        gatewayDiscovery.setGatewayName(gatewayName);
        if(gatewayCustomerName != null && gatewayCustomerName.length() > 0)
            gatewayDiscovery.setCustomerName(gatewayCustomerName);
        if (gatewayCustomerId != null && gatewayCustomerId.trim().length() > 0){
            gatewayCustomerId = gatewayCustomerId.trim();
            s_logger.info("GatewayCustomerId is {}, and {}", gatewayCustomerId, Long.parseLong(gatewayCustomerId));
            gatewayDiscovery.setCustomerId(Long.parseLong(gatewayCustomerId));
        }
        gatewayDiscovery.setLocation(gatewayLocation);
        gatewayDiscovery.setCoordinates(gatewayCoordinates);
        //TODO: Check if there is any device connected
        GatewayDiscoveryProto.GatewayDiscovery gatewayDiscoveryProto =
                gatewayDiscoverySerializer.serializeGatewayDiscovery(gatewayDiscovery);
        UUID uuid = UUID.randomUUID();
        String gatewayInfoTopic = PREFIX_TOPIC + gatewayName + SUFFIX_INFO_TOPIC + uuid;
        publishInformation(gatewayDiscoveryProto, gatewayInfoTopic);

        GatewayDiscoveryMessageCallback callback = new GatewayDiscoveryMessageCallback((DeviceReadingsResponseDeserializer) ApplicationContextHandler.getBean(DeviceReadingsResponseDeserializer.class));
        callback.setUuid(uuid);
        Timestamp discoveryTimestamp = new Timestamp(new Date().getTime());
        publisherCallbackFactory.setDiscoveryRespExpirationTs(discoveryTimestamp);
        subscribeResponse(gatewayInfoTopic, callback);
    }

    public GatewayDataProto.DevicesReadingsFromGateway serializeDeviceReadingsFromGateway(DeviceReadingsFromGateway deviceReadingsFromGateway){
        GatewayDataProto.DevicesReadingsFromGateway deviceReadingsFromGatewayProto =
                devicesReadingsFromGatewaySerializer.serializeDevicesData(deviceReadingsFromGateway);
        return deviceReadingsFromGatewayProto;
    }

    public void publishInformation(Object protoObject, String topic) throws IOException, MqttException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(byteOutputStream);
        os.writeObject(protoObject);
        s_logger.info("ClientId {} publishing data on topic {}",
                mqttClient.getClientId(), topic);
        MqttMessage mqttMessage = new MqttMessage(byteOutputStream.toByteArray());
        mqttMessage.setQos(1);
        mqttMessage.setId(alarmId.get());
        mqttClient.publish(topic, mqttMessage);
    }

    public String createAlarmTopic(UUID uuid) {
        return PREFIX_TOPIC + gatewayName + SUFFIX_DATA_TOPIC + uuid;
    }

    private void subscribeResponse(String gatewayInfoTopic, IMqttMessageListener callback) throws  MqttException{
        String responseTopic = gatewayInfoTopic + "/response";
        mqttClient.subscribe(responseTopic, callback);
    }

    private boolean checkIfDataToSend() {
        //TODO: Implement code to check if there is data to send
        return true;
    }

    private DeviceData createDeviceData() {
        //TODO: TO be removed. Fake data
        DeviceData deviceData = new DeviceData();
        deviceData.setDeviceName(Integer.toString(alarmId.getAndIncrement()));
        deviceData.setTimestamp(new Timestamp(new Date().getTime()));
        DeviceReading reading = new DeviceReading();
        List<DeviceReading> deviceReadingList =  new ArrayList<>();
        reading.setReadingType(ReadingType.SENSOR_CURRENT_VALUE);
        reading.setValue(String.valueOf(alarmId.intValue() + 2));
        deviceReadingList.add(reading);
        deviceData.setDeviceReadings(deviceReadingList);
        s_logger.info("CREATED FAKE DEVICE DATA FOR DEVICE ID {}: {}.",
                deviceData.getDeviceName(), deviceData.getTimestamp());
        return deviceData;
    }

    public void closePublisher() {
        try {
            s_logger.info("Shutting down the mqtt gateway {}", gatewayName);
            mqttClient.disconnect();
            mqttClient.close();
        } catch (MqttException e) {
            s_logger.trace("Exception received while closing the mqtt client");
        }
    }

    private void setConnectOptions() {
        s_logger.info("Setting the connect options");
        options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setUserName(gatewayName);
        options.setKeepAliveInterval(60);
        options.setConnectionTimeout(300);
      //  options.setAutomaticReconnect(true);
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
        try {
            options.setSocketFactory(SslUtilDigirise.getSocketFactory("src\\main\\resources\\certs\\ca.crt",
                    "src\\main\\resources\\certs\\client.crt","src\\main\\resources\\certs\\client.key", ""));
        //    options.setSocketFactory(SslUtilDigirise.getSocketFactory(caCert, clientCert, clientKey, ""));
        } catch (Exception e) {
            s_logger.error("Error in setting the socket factory");
            e.printStackTrace();
        }
    }

    public static String asString(Resource resource) {
        try {
            Reader reader = new InputStreamReader(resource.getInputStream());
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


/*    private static SSLSocketFactory getSocketFactory(final Resource caCrtFile, final Resource crtFile,
                                                     final Resource keyFile, final String password)
            throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        // load CA certificate
        X509Certificate caCert = null;

//        FileInputStream fis = new FileInputStream(caCrtFile);
//        BufferedInputStream bis = new BufferedInputStream(fis);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

//        while (bis.available() > 0) {
//            caCert = (X509Certificate) cf.generateCertificate(bis);
//            // System.out.println(caCert.toString());
//        }

        caCert = (X509Certificate) cf.generateCertificate(caCrtFile.getInputStream());

        // load client certificate
//        bis = new BufferedInputStream(new FileInputStream(crtFile));
        X509Certificate cert = null;
//        while (bis.available() > 0) {
//            cert = (X509Certificate) cf.generateCertificate(bis);
//            // System.out.println(caCert.toString());
//        }
        cert = (X509Certificate) cf.generateCertificate(crtFile.getInputStream());

        // load client private key, or try a pure path to see if that works!!
        Security.addProvider(new BouncyCastleProvider());
        String current = new java.io.File( "." ).getCanonicalPath();
        s_logger.info("Current dir:"+current);
        String currentDir = System.getProperty("user.dir");
        s_logger.info("Current dir using System:" +currentDir);
 //       PEMParser pemParser = new PEMParser(new FileReader(keyFile.getFile().getPath()));
 //       PEMParser pemParser = new PEMParser(new FileReader("src/main/resources/certs/client.key"));
        s_logger.info("Key file is {}", keyFile);
        BufferedReader br = new BufferedReader(new InputStreamReader(keyFile.getInputStream()));
        PEMParser pemParser = new PEMParser(br);
        Object object = pemParser.readObject();
 //       PEMKeyPair object = (PEMKeyPair) pemParser.readObject();
        PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder()
                .build(password.toCharArray());
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter()
                .setProvider("BC");
        KeyPair key;
        if (object instanceof PEMEncryptedKeyPair) {
            s_logger.info("Encrypted key - we will use provided password");
            key = converter.getKeyPair(((PEMEncryptedKeyPair) object)
                    .decryptKeyPair(decProv));
        } else {
            s_logger.info("Unencrypted key - no password needed");
            PEMKeyPair pemKeyPair = (PEMKeyPair) object;
            key = converter.getKeyPair(pemKeyPair);
        }
        pemParser.close();

        // CA certificate is used to authenticate server
        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, null);
        caKs.setCertificateEntry("ca-certificate", caCert);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init(caKs);

        // client key and certificates are sent to server so it can authenticate
        // us
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("certificate", cert);
        ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(),
                new java.security.cert.Certificate[] { cert });
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
                .getDefaultAlgorithm());
        kmf.init(ks, password.toCharArray());

        // finally, create SSL socket factory
        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return context.getSocketFactory();
    } */

}
