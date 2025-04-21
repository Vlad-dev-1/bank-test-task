package com.example.messagingapp.service.impl;

import com.example.messagingapp.dto.MessageRequest;
import com.example.messagingapp.dto.MessageResponse;
import com.example.messagingapp.dto.mapper.MapperMessage;
import com.example.messagingapp.entity.Message;
import com.example.messagingapp.entity.MessageStatus;
import com.example.messagingapp.exception.MessagesNotFound;
import com.example.messagingapp.exception.MessageNotFoundById;
import com.example.messagingapp.kafka.producer.KafkaMessageProducer;
import com.example.messagingapp.repository.MessageRepository;
import com.example.messagingapp.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;

    private final MapperMessage mapperMessage;

    private final KafkaMessageProducer kafkaMessageProducer;


    @Override
    @Transactional
    public MessageResponse saveMessage(MessageRequest messageRequest) {

        Message message = mapperMessage.mapperMessageRequestDto(messageRequest);
        kafkaMessageProducer.sendMessageRequest(messageRequest);
        MessageStatus[] statuses = MessageStatus.values();
        Message messageByStatus = null;

        try {

            for (int i = 0; i < statuses.length - 1; i++) {
                messageByStatus = Message.builder().id(message.getId())
                        .content(message.getContent())
                        .status(statuses[i])
                        .timestamp(message.getTimestamp())
                        .processedAt(Instant.now())
                        .build();
                messageRepository.save(messageByStatus);

                Thread.sleep(1000);
            }

            MessageResponse messageResponse = mapperMessage.mapperMessage(messageByStatus);
            kafkaMessageProducer.sendMessageResponse(messageResponse);
            return messageResponse;

        } catch (Exception e) {
            message.setStatus(MessageStatus.FAILED);
            message.setProcessedAt(Instant.now());
            messageRepository.save(message);
            MessageResponse messageResponse = mapperMessage.mapperMessage(message);
            kafkaMessageProducer.sendMessageResponse(messageResponse);
            return messageResponse;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages() {

        List<MessageResponse> allMessage = mapperMessage.getAllMessage(messageRepository.findAll());
        if (!allMessage.isEmpty()) {
            return allMessage;
        } else {
            throw new MessagesNotFound(MessagesNotFound.MESSAGES_NOT_FOUND);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponse getMessageByID(UUID idMessage) {
        Message message = messageRepository.findById(idMessage).orElseThrow(() ->
                new MessageNotFoundById(MessageNotFoundById.MESSAGE_NOT_FOUND_BY_ID));
        return mapperMessage.mapperMessage(message);
    }
}
