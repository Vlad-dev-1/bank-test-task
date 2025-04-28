package com.example.messagingapp.service.impl;


import com.example.messagingapp.dto.MessageStatisticResponse;
import com.example.messagingapp.exception.MessageStatisticResponseException;
import com.example.messagingapp.repository.MessageRepository;
import com.example.messagingapp.service.MessageStatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

import static com.example.messagingapp.exception.MessageStatisticResponseException.MESSAGE_STATISTIC_EXCEPTION;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageStatisticServiceImpl implements MessageStatisticService {

    private final MessageRepository messageRepository;

    private final Resilience4JCircuitBreakerFactory circuitBreakerFactory;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "messages", key = "'statistics'", unless = "#result == null")
    public MessageStatisticResponse getMessageStatistic() {

        return circuitBreakerFactory.create("messageStatisticService").run(() -> {
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
            } catch (Exception e) {
                log.error("Произошла ошибка при получении статистики сообщений", e);
                throw new MessageStatisticResponseException(MESSAGE_STATISTIC_EXCEPTION);
            }
        }, throwable -> {
            log.error("Автоматический выключатель, срабатывающий для получения статистических данных о сообщениях," +
                    " метод {}: {}", "getMessageStatistic", throwable.getMessage());
            throw new MessageStatisticResponseException(MESSAGE_STATISTIC_EXCEPTION);
        });
    }
}
