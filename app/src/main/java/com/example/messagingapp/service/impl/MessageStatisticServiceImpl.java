package com.example.messagingapp.service.impl;


import com.example.messagingapp.dto.MessageStatisticResponse;
import com.example.messagingapp.exception.MessageStatisticResponseException;
import com.example.messagingapp.repository.MessageRepository;
import com.example.messagingapp.service.MessageStatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "messages")
public class MessageStatisticServiceImpl implements MessageStatisticService {

    private final MessageRepository messageRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'statistics'", unless = "#result == null")
    public MessageStatisticResponse getMessageStatistic() {
        try {
            MessageStatisticResponse messageStatisticResponse = new MessageStatisticResponse(messageRepository.
                    findAll().
                    stream().
                    collect(Collectors.toMap(message -> message.
                                    getStatus().
                                    name(),
                            count -> 1L, Long::sum)));
            log.info("Статистика сообщений собрана: {}", messageStatisticResponse);
            return messageStatisticResponse;
        }catch (Exception e){
            log.error("Произошла ошибка при получении статистики сообщений",e);
            throw new MessageStatisticResponseException(MessageStatisticResponseException.MESSAGE_STATISTIC_EXCEPTION);
        }
    }
}
