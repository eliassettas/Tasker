package com.example.tasker.service;

import java.util.Collections;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import com.example.tasker.exception.CustomException;
import com.example.tasker.model.message.ChatMessage;
import com.example.tasker.model.persistence.SystemUser;
import com.example.tasker.model.persistence.UserProfile;
import com.example.tasker.repository.ChatMessageRepository;
import com.example.tasker.util.UserUtils;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final SystemUserService systemUserService;

    public ChatMessageService(
            ChatMessageRepository chatMessageRepository,
            SystemUserService systemUserService
    ) {
        this.chatMessageRepository = chatMessageRepository;
        this.systemUserService = systemUserService;
    }

    public ChatMessage save(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> getPreviousMessages(String currentMessageId) throws CustomException {
        List<ChatMessage> messages = currentMessageId != null && !currentMessageId.isEmpty()
                ? chatMessageRepository.findTop20ByIdIsLessThanOrderByIdDesc(new ObjectId(currentMessageId))
                : chatMessageRepository.findTop20ByOrderByIdDesc();
        Collections.reverse(messages);
        for (ChatMessage message : messages) {
            SystemUser systemUser = systemUserService.getUserById(message.getSenderId());
            UserProfile userProfile = systemUser.getProfile();
            String name = UserUtils.constructName(userProfile.getFirstName(), userProfile.getLastName());
            message.setSenderName(name);
        }
        return messages;
    }
}
