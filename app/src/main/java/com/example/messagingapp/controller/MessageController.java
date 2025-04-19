package com.example.messagingapp.controller;

import com.example.messagingapp.dto.MessageRequestDto;
import com.example.messagingapp.dto.MessageResponseDto;
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
    public MessageResponseDto saveMessage(@RequestBody MessageRequestDto messageRequestDto) {
        return messageService.saveMessage(messageRequestDto);
    }

    @GetMapping
    public List<MessageResponseDto> getAllMessages() {
        return messageService.getMessages();
    }

    @GetMapping("/{id}")
    public MessageResponseDto getMessageByID(@PathVariable("id") UUID messageID) {
        return messageService.getMessageByID(messageID);
    }
}
