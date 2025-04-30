package com.example.messagingapp.service.impl;

import com.example.messagingapp.dto.MessageRequest;
import com.example.messagingapp.dto.MessageResponse;
import com.example.messagingapp.mapper.MapperMessage;
import com.example.messagingapp.entity.Message;
import com.example.messagingapp.entity.MessageStatus;
import com.example.messagingapp.exception.MessageExistsToDataBase;
import com.example.messagingapp.exception.MessagesNotFound;
import com.example.messagingapp.exception.MessageNotFoundById;
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
import static com.example.messagingapp.exception.MessageNotFoundById.MESSAGE_NOT_FOUND_BY_ID;
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
    @CacheEvict(cacheNames = "messages", allEntries = true)
    public MessageResponse saveMessage(MessageRequest messageRequest) {

        try {
            Optional<Message> messageById = messageRepository.findMessageById(messageRequest.getId());
            if (messageById.isPresent() && messageById.get().getStatus().equals(MessageStatus.PROCESSED)) {
                throw new MessageExistsToDataBase(MESSAGE_EXISTS_TO_DB);
            }
        } catch (MessageExistsToDataBase e) {
            log.error("Сообщение с таким ID уже обработано", e);
            throw new MessageExistsToDataBase(MESSAGE_EXISTS_TO_DB);
        }

        return circuitBreakerFactory.create("messageService").run(() -> {

            try {
                log.info("Начало обработки сообщения: {}", messageRequest.getId());
                Message message = mapperMessage.mapperMessageRequestToMessage(messageRequest);
                kafkaMessageProducer.sendMessageRequest(messageRequest);
                log.info("Отправка входящего сообщения в Kafka. ID сообщения: {}", messageRequest.getId());

                //логика обработки сообщения
                Thread.sleep(50);
                message.setStatus(MessageStatus.PROCESSED);
                message.setProcessedAt(Instant.now());
                messageRepository.save(message);
                log.info("Сохранение успешно обработанного сообщения в базу данных с ID: {} статус: {}",
                        message.getId(), message.getStatus());

                MessageResponse messageResponse = mapperMessage.mapperMessageToMessageResponse(message);
                kafkaMessageProducer.sendMessageResponse(messageResponse);
                log.info("Отправка успешно обработанного сообщения в Kafka с ID: {}, статус {}", messageResponse.getMessageId(),
                        messageResponse.getStatus());
                return messageResponse;

            } catch (Exception e) {
                log.error("Ошибка обработки сообщения с ID: {}. Причина: {}", messageRequest.getId(), e.getMessage(), e);
                return messageSaveInError.messageSaveInError(messageRequest);
            }
        }, throwable -> {
            log.error("Автоматический выключатель, срабатывающий для сохранения сообщения," +
                    " метод {}: {}", "saveMessage", throwable.getMessage());
            return messageSaveInError.messageSaveInError(messageRequest);
        });
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "messages")
    public List<MessageResponse> getMessages() {

        return circuitBreakerFactory.create("messageService").run(() -> {

            List<MessageResponse> allMessage = mapperMessage.getAllMessageResponse(messageRepository.findAll());
            if (!allMessage.isEmpty()) {
                log.info("Найдено {} сообщений", allMessage.size());
                return allMessage;
            } else {
                log.warn("Сообщения не найдены в базе данных");
                throw new MessagesNotFound(MESSAGES_NOT_FOUND);
            }
        }, throwable -> {
            log.error("Автоматический выключатель, срабатывающий для получения сообщений," +
                    " метод {} :  {}", "getMessages", throwable.getMessage());
            throw new MessagesNotFound(MESSAGES_NOT_FOUND);
        });
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "messages", key = "#idMessage", unless = "#result == null")
    public MessageResponse getMessageByID(UUID idMessage) {

        return circuitBreakerFactory.create("messageService").run(() -> {

            Message message = messageRepository.findById(idMessage).orElseThrow(() ->
                    new MessageNotFoundById(MESSAGE_NOT_FOUND_BY_ID));
            log.info("Найдено сообщение с ID: {}, контент {}, со статусом: {}", idMessage,
                    message.getContent(),
                    message.getStatus());
            return mapperMessage.mapperMessageToMessageResponse(message);
        }, throwable -> {
            log.error("Автоматический выключатель, срабатывающий для получения сообщений по ID," +
                    " метод {} : {}", "getMessageByID", throwable.getMessage());
            throw new MessageNotFoundById(MESSAGE_NOT_FOUND_BY_ID);
        });
    }
}
