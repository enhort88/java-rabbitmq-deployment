package com.example.rabbitmqdemo.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import org.springframework.amqp.core.Queue;


@Configuration
@RequiredArgsConstructor
public class RabbitConfig {

    private final Environment env;

    @Bean
    public Queue queue() {
        return QueueBuilder.durable(env.getProperty("rabbit.queue"))
                .withArgument("x-dead-letter-exchange", "dlx-exchange")
                .withArgument("x-dead-letter-routing-key", "dlq-routing-key")
                .build();
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(env.getProperty("rabbit.exchange"));
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange("dlx-exchange");
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(env.getProperty("rabbit.dlq"))
                .build();
    }


    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(dlxExchange())
                .with("dlq-routing-key");
    }

    @Bean
    public Queue archiveQueue() {
        return QueueBuilder.durable(env.getProperty("rabbit.arch")).build(); // ✅ используем переменную
    }

    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(env.getProperty("rabbit.routing-key"));
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

}
