package com.example.tasker.service;

import org.springframework.stereotype.Service;

import com.example.tasker.model.message.ChatMessage;
import com.example.tasker.repository.ChatMessageRepository;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    public ChatMessage save(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }
}
