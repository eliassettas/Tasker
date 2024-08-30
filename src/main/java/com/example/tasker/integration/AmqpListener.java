package com.example.tasker.integration;

import javax.transaction.Transactional;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.example.tasker.exception.CustomException;
import com.example.tasker.model.mail.MailRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AmqpListener {

    private final ObjectMapper objectMapper;
    private final EmailSender emailSender;

    public AmqpListener(ObjectMapper objectMapper, EmailSender emailSender) {
        this.objectMapper = objectMapper;
        this.emailSender = emailSender;
    }

    @Transactional(rollbackOn = CustomException.class)
    @RabbitListener(queues = "${constants.account-activation-queue}")
    public void receiveAccountActivationRequest(String request) throws CustomException {
        MailRequest mailRequest;
        try {
            mailRequest = objectMapper.readValue(request, MailRequest.class);
        } catch (JsonProcessingException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Failed to deserialize message from broker!");
        }
        emailSender.sendMail(mailRequest);
    }
}
