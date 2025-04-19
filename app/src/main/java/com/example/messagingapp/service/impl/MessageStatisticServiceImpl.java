package com.example.messagingapp.service.impl;

import com.example.messagingapp.entity.Message;
import com.example.messagingapp.entity.MessageStatus;
import com.example.messagingapp.repository.MessageRepository;
import com.example.messagingapp.service.MessageStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageStatisticServiceImpl implements MessageStatisticService {

    private final MessageRepository messageRepository;

    @Override
    public Map<MessageStatus, Long> getMessageStatistic() {
        List<Message> messageList = messageRepository.findAll();
        Map<MessageStatus, Long> collect = null;
        if (Objects.nonNull(messageList)) {
            return collect = messageList.stream()
                    .collect(Collectors.toMap(Message::getStatus, count -> 1L, Long::sum));
        }
        return new HashMap<>();
    }
}
