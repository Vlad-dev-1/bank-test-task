package com.example.messagingapp.service.impl;

import com.example.messagingapp.dto.MessageRequest;
import com.example.messagingapp.dto.MessageResponse;
import com.example.messagingapp.mapper.MapperMessage;
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
    public MessageResponse messageSaveInError(MessageRequest messageRequest) {
        log.warn("Попытка сохранения входящего не обработанного сообщения в статусе FAILED." +
                        " ID: {}, время получения {}",
                messageRequest.getId(), messageRequest.getTimestamp());
        Message message = mapperMessage.mapperMessageRequestToMessage(messageRequest);
        message.setStatus(MessageStatus.FAILED);
        message.setProcessedAt(Instant.now());
        Message saveMessage = messageRepository.save(message);
        log.info("Не обработанное сообщение сохранено с ID: {}, статус: {}",
                saveMessage.getId(), saveMessage.getStatus());

        MessageResponse messageResponse = mapperMessage.mapperMessageToMessageResponse(saveMessage);
        kafkaMessageProducer.sendMessageResponse(messageResponse);
        log.info("Не обработанное сообщение отправлено в Kafka. ID сообщения: {}, статус {}",
                messageResponse.getMessageId(), messageResponse.getStatus());
        return messageResponse;
    }
}
