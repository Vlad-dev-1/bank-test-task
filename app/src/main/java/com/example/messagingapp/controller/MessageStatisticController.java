package com.example.messagingapp.controller;

import com.example.messagingapp.entity.MessageStatus;
import com.example.messagingapp.service.MessageStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statistics")
public class MessageStatisticController {

    private final MessageStatisticService messageStatisticService;

    @GetMapping
    public Map<MessageStatus, Long> getMessagesStatistic() {
        return messageStatisticService.getMessageStatistic();
    }
}
