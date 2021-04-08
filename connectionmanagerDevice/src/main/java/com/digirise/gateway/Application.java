package com.digirise.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import javax.annotation.Resource;

@SpringBootApplication
@Configuration
@ComponentScan({"com.digirise.gateway", "com.digirise.sai.commons"})
//@PropertySource("classpath:application.properties") Application.properties get read by default
//@PropertySource("file:${properties.home}/gateway.properties") <read using spring.config.
public class Application {
    private static final Logger s_logger = LoggerFactory.getLogger(Application.class);
//    @Resource
//    private Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        s_logger.info("classpath is {}", System.getProperty("java.class.path"));
        s_logger.info("Started the device connection manager");
    }

    //To resolve ${} in @Value
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
