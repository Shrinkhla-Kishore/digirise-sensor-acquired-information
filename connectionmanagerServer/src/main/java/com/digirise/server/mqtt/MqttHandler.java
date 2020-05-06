package com.digirise.server.mqtt;

import com.digirise.server.mqtt.receiver.Subscriber;
import com.digirise.server.mqtt.sender.FirmwareDispatcher;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class MqttHandler {
    private FirmwareDispatcher firmwareDispatcher;
    ThreadPoolExecutor executor;

    public MqttHandler(FirmwareDispatcher firmwareDispatcher, @org.jetbrains.annotations.NotNull Subscriber subscriber) {
        subscriber.configureSubscriber();
        executor = new ScheduledThreadPoolExecutor(1);
       ((ScheduledThreadPoolExecutor) executor).scheduleWithFixedDelay(subscriber, 2000, 5000, TimeUnit.MILLISECONDS);
    }
}
