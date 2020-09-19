package com.digirise.server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-31
 * Author: shrinkhlak
 */

@Component
public class DataProcessingHandler {
    private static final Logger s_logger = LoggerFactory.getLogger(DataProcessingHandler.class);
    private ExecutorService dpExecutorService;

    public DataProcessingHandler() {
        dpExecutorService = Executors.newFixedThreadPool(10);
    }

    public ExecutorService getDpExecutorService() {
        return dpExecutorService;
    }
}
