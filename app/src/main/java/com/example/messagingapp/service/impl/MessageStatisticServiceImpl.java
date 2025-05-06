package com.example.messagingapp.service.impl;


import com.example.messagingapp.dto.MessageStatisticResponse;
import com.example.messagingapp.exception.MessageStatisticException;
import com.example.messagingapp.exception.MessageStatisticIsEmpty;
import com.example.messagingapp.repository.MessageRepository;
import com.example.messagingapp.service.MessageStatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

import static com.example.messagingapp.exception.MessageStatisticException.MESSAGE_STATISTIC_EXCEPTION;
import static com.example.messagingapp.exception.MessageStatisticIsEmpty.MESSAGE_STATISTIC_IS_EMPTY;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageStatisticServiceImpl implements MessageStatisticService {

    private final MessageRepository messageRepository;

    private final Resilience4JCircuitBreakerFactory circuitBreakerFactory;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "messages_cache", key = "'statistics'", unless = "#result == null")
    public MessageStatisticResponse getMessageStatistic() {

        return circuitBreakerFactory.create("messageStatisticService").run(() -> {

            MessageStatisticResponse messageStatisticResponse = new MessageStatisticResponse(messageRepository.
                    findAll().
                    stream().
                    collect(Collectors.toMap(message -> message.getStatus().name(), count -> 1L, Long::sum)));
            if (messageStatisticResponse.getStatisticMessages().isEmpty()) {
                log.warn("Статистика сообщений не найдена");
                throw new MessageStatisticIsEmpty(MESSAGE_STATISTIC_IS_EMPTY);
            }
            log.info("Статистика сообщений собрана: {}", messageStatisticResponse);
            return messageStatisticResponse;
        }, exception -> {
            if (exception instanceof MessageStatisticIsEmpty) {
                throw new MessageStatisticIsEmpty(MESSAGE_STATISTIC_IS_EMPTY);
            }
            log.error("Автоматический выключатель, ошибка при получении статистики сообщений" +
                    " метод {}: {}", "getMessageStatistic", exception.getMessage(), exception);
            throw new MessageStatisticException(MESSAGE_STATISTIC_EXCEPTION);
        });
    }
}
