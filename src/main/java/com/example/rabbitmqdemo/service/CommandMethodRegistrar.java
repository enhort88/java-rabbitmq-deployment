package com.example.rabbitmqdemo.service;

import com.example.rabbitmqdemo.meta.CommandMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CommandMethodRegistrar implements org.springframework.beans.factory.SmartInitializingSingleton {

    private final ApplicationContext context;
    private final CommandDispatcher dispatcher;

    @Override
    public void afterSingletonsInstantiated() {
        Map<String, Object> beans = context.getBeansWithAnnotation(Component.class);

        for (Object bean : beans.values()) {
            Class<?> targetClass = AopUtils.getTargetClass(bean);

            for (Method method : targetClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(CommandMethod.class)) {
                    String name = method.getName();
                    CommandMethod annotation = method.getAnnotation(CommandMethod.class);
                    method.setAccessible(true);
                    dispatcher.register(name, bean, method);
                }
            }
        }
    }
}

