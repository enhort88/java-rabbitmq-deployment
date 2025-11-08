package com.example.rabbitmqdemo.service;

import com.example.rabbitmqdemo.dto.CommandMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.core.env.Environment;

@Component
@Log4j2
@RequiredArgsConstructor
public class DeadLetterListener {
    private final AmqpTemplate rabbitTemplate;
    private final Environment env;


    @RabbitListener(queues = "${rabbit.dlq}") // добавим в application.properties
    public void onDeadLetter(CommandMessage message) {
        log.warn("🐞 Получено сообщение из DLQ: method={}, params={}", message.getMethod(), message.getParams());
        String archiveQueue = env.getProperty("rabbit.arch");
        rabbitTemplate.convertAndSend(archiveQueue, message);
        log.warn("Ошибка отправлена в архив \uD83D\uDCE4🐞");
     }

}
