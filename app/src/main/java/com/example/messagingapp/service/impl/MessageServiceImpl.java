package com.example.messagingapp.service.impl;

import com.example.messagingapp.dto.MessageRequest;
import com.example.messagingapp.dto.MessageResponse;
import com.example.messagingapp.exception.*;
import com.example.messagingapp.mapper.MapperMessage;
import com.example.messagingapp.entity.Message;
import com.example.messagingapp.entity.MessageStatus;
import com.example.messagingapp.kafka.producer.KafkaMessageProducer;
import com.example.messagingapp.repository.MessageRepository;
import com.example.messagingapp.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.example.messagingapp.exception.MessageExistsToDataBase.MESSAGE_EXISTS_TO_DB;
import static com.example.messagingapp.exception.MessageGetByIDException.MESSAGE_GET_BY_ID_EXCEPTION;
import static com.example.messagingapp.exception.MessageNotFoundById.MESSAGE_NOT_FOUND_BY_ID;
import static com.example.messagingapp.exception.MessagesGetException.MESSAGES_GET_EXCEPTION;
import static com.example.messagingapp.exception.MessagesNotFound.MESSAGES_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;

    private final MapperMessage mapperMessage;

    private final KafkaMessageProducer kafkaMessageProducer;

    private final MessageSaveInError messageSaveInError;

    private final Resilience4JCircuitBreakerFactory circuitBreakerFactory;

    @Override
    @Transactional
    @CacheEvict(cacheNames = "messages_cache", allEntries = true)
    public MessageResponse saveMessage(MessageRequest messageRequest) {

        return circuitBreakerFactory.create("messageService").run(() -> {

            Optional<Message> messageById = messageRepository.findMessageById(messageRequest.getId());
            if (messageById.isPresent() && messageById.get().getStatus().equals(MessageStatus.PROCESSED)) {
                log.warn("Сообщение с таким ID {} уже обработано", messageById.get().getId());
                throw new MessageExistsToDataBase(MESSAGE_EXISTS_TO_DB);
            }

            log.info("Начало обработки сообщения: {}", messageRequest.getId());
            Message message = mapperMessage.mapperMessageRequestToMessage(messageRequest);
            kafkaMessageProducer.sendMessageRequest(messageRequest);
            log.info("Отправка входящего сообщения в Kafka. ID сообщения: {}", messageRequest.getId());

            try {
                //логика обработки сообщения
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            message.setStatus(MessageStatus.PROCESSED);
            message.setProcessedAt(Instant.now());
            Message saveMessage = messageRepository.save(message);
            log.info("Сохранение успешно обработанного сообщения в базу данных с ID: {} статус: {}",
                    saveMessage.getId(), saveMessage.getStatus());

            MessageResponse messageResponse = mapperMessage.mapperMessageToMessageResponse(saveMessage);
            kafkaMessageProducer.sendMessageResponse(messageResponse);
            log.info("Отправка успешно обработанного сообщения в Kafka с ID: {}, статус {}",
                    messageResponse.getMessageId(),
                    messageResponse.getStatus());
            return messageResponse;

        }, exception -> {
            if (exception instanceof MessageExistsToDataBase) {
                throw new MessageExistsToDataBase(MESSAGE_EXISTS_TO_DB);
            }
            log.error("Автоматический выключатель, ошибка обработки сообщений с ID: {}. Метод {} : {}",
                    messageRequest.getId(),
                    "saveMessage",
                    exception.getMessage(),
                    exception);
            return messageSaveInError.messageSaveInError(messageRequest);
        });
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "messages_cache", unless = "#result == null")
    public List<MessageResponse> getMessages() {

        return circuitBreakerFactory.create("messageService").run(() -> {

            List<MessageResponse> allMessage = mapperMessage.getAllMessageResponse(messageRepository.findAll());
            if (allMessage.isEmpty()) {
                log.warn("Сообщения не найдены в базе данных");
                throw new MessagesNotFound(MESSAGES_NOT_FOUND);
            }
            log.info("Найдено {} сообщений", allMessage.size());
            return allMessage;

        }, exception -> {
            if (exception instanceof MessagesNotFound) {
                throw new MessagesNotFound(MESSAGES_NOT_FOUND);
            }
            log.error("Автоматический выключатель, ошибка получения сообщений," +
                    " метод {} :  {}", "getMessages", exception.getMessage(), exception);
            throw new MessagesGetException(MESSAGES_GET_EXCEPTION);
        });
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "messages_cache", key = "#idMessage", unless = "#result == null")
    public MessageResponse getMessageByID(UUID idMessage) {

        return circuitBreakerFactory.create("messageService").run(() -> {

            Optional<Message> messageById = messageRepository.findById(idMessage);
            if (messageById.isEmpty()) {
                log.warn("Не найдено сообщение по ID {} в базе данных", idMessage);
                throw new MessageNotFoundById(MESSAGE_NOT_FOUND_BY_ID);
            }
            log.info("Найдено сообщение с ID: {}, статус: {}",
                    messageById.get().getId(),
                    messageById.get().getStatus());
            return mapperMessage.mapperMessageToMessageResponse(messageById.get());
        }, exception -> {
            if (exception instanceof MessageNotFoundById) {
                throw new MessageNotFoundById(MESSAGE_NOT_FOUND_BY_ID);
            }
            log.error("Автоматический выключатель, ошибка получения сообщения по ID {}," +
                    " метод {} : {}", idMessage, "getMessageByID", exception.getMessage(), exception);
            throw new MessageGetByIDException(MESSAGE_GET_BY_ID_EXCEPTION);
        });
    }
}
