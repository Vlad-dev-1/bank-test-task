package com.example.messagingapp.service.impl;


import com.example.messagingapp.entity.MessageStatus;
import com.example.messagingapp.repository.MessageRepository;
import com.example.messagingapp.service.MessageStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.Map;


@Service
@RequiredArgsConstructor
public class MessageStatisticServiceImpl implements MessageStatisticService {

    private final MessageRepository messageRepository;

    @Override
    public Map<MessageStatus, Long> getMessageStatistic() {
        return messageRepository.countMessagesByStatus();
    }
}
