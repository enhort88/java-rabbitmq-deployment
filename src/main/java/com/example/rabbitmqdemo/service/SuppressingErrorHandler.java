package com.example.rabbitmqdemo.service;

import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.stereotype.Component;


@Component("suppressingErrorHandler")
@Log4j2
public class SuppressingErrorHandler implements RabbitListenerErrorHandler {

    @Override
    public Object handleError(
            Message amqpMessage,
            Channel channel,
            org.springframework.messaging.Message<?> springMessage,
            ListenerExecutionFailedException exception) {

        Throwable cause = exception.getCause();
        log.warn("💥 Ошибка в слушателе: {}", cause != null ? cause.getMessage() : exception.getMessage());
        throw exception;
    }
}
