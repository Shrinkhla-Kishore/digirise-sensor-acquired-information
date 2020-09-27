package com.digirise.dataprocessing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
@ComponentScan("com.digirise.dataprocessing")
public class Application implements CommandLineRunner {
    private static final Logger s_logger = LoggerFactory.getLogger(Application.class);
    @Autowired
    private ApplicationContext applicationContext;

    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
        s_logger.info("DATA PROCESSING SERVER STARTED");
    }

//    public static void displayAllBeans() {
//        String[] beans = applicationContext.getBeanDefinitionNames();
//        s_logger.info("Beans created so far is:");
//        for (String bean : beans){
//            s_logger.info("{}", bean);
//            System.out.println(bean);
//        }
//    }

    @Override
    public void run(String... args) throws Exception {
        String[] beans = applicationContext.getBeanDefinitionNames();
        s_logger.info("Beans created so far is:");
        for (String bean : beans){
            s_logger.info("{}", bean);
            System.out.println(bean);
        }
    }
}
