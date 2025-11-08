package com.example.rabbitmqdemo.controller;

import com.example.rabbitmqdemo.dto.CommandMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/command")
public class CommandSenderController {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbit.exchange}")
    private String exchange;

    @Value("${rabbit.routing-key}")
    private String routingKey;

    @PostMapping("/send")
    public String sendCommand(@RequestBody CommandMessage message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
        return "✅ Команда отправлена: " + message.getMethod();
    }
}