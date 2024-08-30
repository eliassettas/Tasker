package com.example.tasker.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class AmqpConfig {

    @Bean
    public Queue accountActivationQueue(@Value("${constants.account-activation-queue}") String accountActivationQueue) {
        return new Queue(accountActivationQueue);
    }
}
