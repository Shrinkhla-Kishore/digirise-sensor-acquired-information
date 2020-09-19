package com.digirise.gateway;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-08-13
 * Author: shrinkhlak
 */

@Component
public class ApplicationContextHandler implements ApplicationContextAware {
    private static ApplicationContext s_context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        s_context = applicationContext;
    }

    public static <T extends Object> T getBean(Class<T> beanClass) {
        return s_context.getBean(beanClass);
    }
}
