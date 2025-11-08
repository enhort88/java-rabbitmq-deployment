package com.example.rabbitmqdemo.service;

import com.example.rabbitmqdemo.dto.CommandMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class CommandListener {

    private final CommandDispatcher dispatcher;

    @RabbitListener(queues = "${rabbit.queue}", errorHandler = "suppressingErrorHandler")

    public void onMessage(CommandMessage message) {
        log.info("📨 Получена команда: {}", message.getMethod());
        try {
            dispatcher.dispatch(message.getMethod(), message.getParams());
        } catch (IllegalArgumentException e) {
            log.warn("Неверная команда: {}", e.getMessage());
            throw new AmqpRejectAndDontRequeueException("Невалидная команда — отправка в DLQ");
        } catch (Exception e) {
            log.error("Ошибка при обработке команды: {}", e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("Общая ошибка — отправка в DLQ");
        }
    }
}
