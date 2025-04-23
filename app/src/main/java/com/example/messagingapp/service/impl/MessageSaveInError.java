package com.example.messagingapp.service.impl;

import com.example.messagingapp.dto.MessageResponse;
import com.example.messagingapp.dto.mapper.MapperMessage;
import com.example.messagingapp.entity.Message;
import com.example.messagingapp.entity.MessageStatus;
import com.example.messagingapp.kafka.producer.KafkaMessageProducer;
import com.example.messagingapp.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSaveInError {

    private final MessageRepository messageRepository;

    private final MapperMessage mapperMessage;

    private final KafkaMessageProducer kafkaMessageProducer;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MessageResponse messageSaveInError(Message message) {
        log.warn("Сохранение сообщения в статусе FAILED. ID: {}, контент: {}, время получения {}",
                message.getId(), message.getContent(), message.getTimestamp());
        message.setStatus(MessageStatus.FAILED);
        message.setProcessedAt(Instant.now());
        messageRepository.save(message);
        log.debug("Сообщение сохранено с ID: {}, статус: {}",
                message.getId(), message.getStatus());

        MessageResponse messageResponse = mapperMessage.mapperMessage(message);

        kafkaMessageProducer.sendMessageResponse(messageResponse);
        log.info("Отправлен ответ на сообщение в Kafka. ID сообщения: {}, статус {}", messageResponse.getMessageId(),
                messageResponse.getStatus());
        return messageResponse;
    }
}
