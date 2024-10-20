package com.example.tasker.endpoint;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.tasker.exception.CustomException;
import com.example.tasker.model.message.ChatMessage;
import com.example.tasker.service.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("api/chat-messages")
public class ChatMessageEndpoint {

    private final ChatMessageService chatMessageService;

    public ChatMessageEndpoint(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @Operation(summary = "Displays previous messages from a given message ID. If no current message ID is provided then the most recent messages will be retrieved.")
    @GetMapping("/previous")
    public List<ChatMessage> getPreviousMessages(@RequestParam(value = "currentMessageId", required = false) String currentMessageId) throws CustomException {
        return chatMessageService.getPreviousMessages(currentMessageId);
    }
}
