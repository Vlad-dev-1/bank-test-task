package com.example.messagingapp.controller;

import com.example.messagingapp.dto.MessageRequest;
import com.example.messagingapp.dto.MessageResponse;
import com.example.messagingapp.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public MessageResponse saveMessage(@RequestBody MessageRequest messageRequest) {
        return messageService.saveMessage(messageRequest);
    }

    @GetMapping
    public List<MessageResponse> getAllMessages() {
        return messageService.getMessages();
    }

    @GetMapping("/{id}")
    public MessageResponse getMessageByID(@PathVariable("id") UUID messageID) {
        return messageService.getMessageByID(messageID);
    }
}
