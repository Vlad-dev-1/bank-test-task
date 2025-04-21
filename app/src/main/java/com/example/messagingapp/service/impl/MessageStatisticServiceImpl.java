package com.example.messagingapp.service.impl;


import com.example.messagingapp.dto.MessageStatisticResponse;
import com.example.messagingapp.repository.MessageRepository;
import com.example.messagingapp.service.MessageStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MessageStatisticServiceImpl implements MessageStatisticService {

    private final MessageRepository messageRepository;

    @Override
    public MessageStatisticResponse getMessageStatistic() {
        return new MessageStatisticResponse(messageRepository.
                findAll().
                stream().
                collect(Collectors.toMap(message -> message.
                                getStatus().
                                name(),
                count -> 1L, Long::sum)));
    }
}
