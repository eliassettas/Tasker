package com.example.tasker.controller;

import java.time.LocalDateTime;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import com.example.tasker.model.message.ChatMessage;
import com.example.tasker.model.persistence.SystemUser;
import com.example.tasker.model.persistence.UserProfile;
import com.example.tasker.util.UserUtils;

@Controller
public class WebSocketController {

    @MessageMapping("/globalChat.sendMessage")
    @SendTo("/topic/globalChat.receiveMessage")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor messageHeaderAccessor) {
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) messageHeaderAccessor.getUser();
        if (authentication == null) {
            throw new AccessDeniedException("Failed to authenticate before sending message to broker");
        }

        SystemUser systemUser = (SystemUser) authentication.getPrincipal();
        chatMessage.setSenderId(systemUser.getId());
        UserProfile userProfile = systemUser.getProfile();
        String name = UserUtils.constructName(userProfile.getFirstName(), userProfile.getLastName());
        chatMessage.setSenderName(name);
        chatMessage.setTimestamp(LocalDateTime.now());
        // TODO: Persist the message into a database like MongoDB
        return chatMessage;
    }
}
