package com.example.tasker.integration;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.tasker.exception.CustomException;
import com.example.tasker.service.TemplateEngineService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AmqpListener.class, EmailSender.class, ObjectMapper.class})
public class AmqpListenerTest {

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private JavaMailSender javaMailSender;

    @MockBean
    private TemplateEngineService templateEngineService;

    @Autowired
    private AmqpListener amqpListener;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> simpleMailMessageArgumentCaptor;

    @Test
    public void test() throws CustomException {
        String message = "{\"to\":\"to@gmail.com\",\"from\":\"from@gmail.com\",\"subject\":\"subject\",\"text\":\"text\"}";

        Mockito.doNothing().when(javaMailSender).send(ArgumentMatchers.any(SimpleMailMessage.class));
        Mockito.when(templateEngineService.buildActivationMail("text")).thenReturn("modified text");

        amqpListener.receiveAccountActivationRequest(message);

        Mockito.verify(javaMailSender).send(simpleMailMessageArgumentCaptor.capture());
        Mockito.verifyNoMoreInteractions(javaMailSender);

        Mockito.verify(templateEngineService).buildActivationMail("text");
        Mockito.verifyNoMoreInteractions(templateEngineService);

        SimpleMailMessage simpleMailMessage = simpleMailMessageArgumentCaptor.getValue();
        Assert.assertNotNull(simpleMailMessage.getTo());
        Assert.assertEquals(1, simpleMailMessage.getTo().length);
        Assert.assertEquals("to@gmail.com", simpleMailMessage.getTo()[0]);
        Assert.assertEquals("from@gmail.com", simpleMailMessage.getFrom());
        Assert.assertEquals("subject", simpleMailMessage.getSubject());
        Assert.assertEquals("modified text", simpleMailMessage.getText());
    }
}
