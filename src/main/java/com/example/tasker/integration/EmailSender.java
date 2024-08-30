package com.example.tasker.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.example.tasker.exception.CustomException;
import com.example.tasker.model.mail.MailRequest;
import com.example.tasker.service.TemplateEngineService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class EmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailSender.class);

    @Value("${constants.mail-sender}")
    private String username;

    @Value("${constants.account-activation-queue}")
    private String mailActivationQueue;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngineService templateEngineService;

    public void sendMailToBroker(String address, String subject, String text) throws CustomException {
        MailRequest mailRequest = new MailRequest(address, username, subject, text);
        String message;
        try {
            message = objectMapper.writeValueAsString(mailRequest);
        } catch (JsonProcessingException e) {
            LOGGER.info("Failed to serialize mail request: {}", mailRequest);
            throw new CustomException(HttpStatus.BAD_REQUEST, "Error while creating validation mail");
        }

        LOGGER.info("Sending message to broker: {}", message);
        rabbitTemplate.convertAndSend(mailActivationQueue, message);
    }

    @Async
    public void sendMail(MailRequest mailRequest) {
        String text = templateEngineService.buildActivationMail(mailRequest.getText());
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(mailRequest.getTo());
        simpleMailMessage.setFrom(mailRequest.getFrom());
        simpleMailMessage.setSubject(mailRequest.getSubject());
        simpleMailMessage.setText(text);
        LOGGER.info("Sending mail to: {}", mailRequest.getTo());
        mailSender.send(simpleMailMessage);
    }
}
