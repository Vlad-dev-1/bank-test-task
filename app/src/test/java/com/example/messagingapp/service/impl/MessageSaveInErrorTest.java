package com.example.messagingapp.service.impl;

import com.example.messagingapp.dto.MessageRequest;
import com.example.messagingapp.dto.MessageResponse;
import com.example.messagingapp.entity.Message;
import com.example.messagingapp.entity.MessageStatus;
import com.example.messagingapp.kafka.producer.KafkaMessageProducer;
import com.example.messagingapp.mapper.MapperMessage;
import com.example.messagingapp.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageSaveInErrorTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MapperMessage mapperMessage;

    @Mock
    private KafkaMessageProducer kafkaMessageProducer;

    @InjectMocks
    private MessageSaveInError messageSaveInError;

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void messageSaveInError_ShouldSaveFailedMessage() {

        MessageRequest request = MessageRequest.builder()
                .id(UUID.randomUUID())
                .content("Test content")
                .timestamp(Instant.now())
                .build();

        Message message = Message.builder()
                .id(request.getId())
                .content(request.getContent())
                .timestamp(request.getTimestamp())
                .status(MessageStatus.FAILED)
                .processedAt(Instant.now().plusSeconds(15))
                .build();

        MessageResponse response = MessageResponse.builder()
                .messageId(message.getId())
                .status(message.getStatus())
                .processedAt(message.getProcessedAt())
                .build();

        when(mapperMessage.mapperMessageRequestToMessage(request)).thenReturn(message);
        when(messageRepository.save(message)).thenReturn(message);
        when(mapperMessage.mapperMessageToMessageResponse(message)).thenReturn(response);

        MessageResponse result = messageSaveInError.messageSaveInError(request);

        assertEquals(message.getStatus(), result.getStatus());
        assertNotNull(message.getProcessedAt());
        assertEquals(response, result);
        verify(messageRepository, times(1)).save(message);
        verify(kafkaMessageProducer, times(1)).sendMessageResponse(response);
    }
}