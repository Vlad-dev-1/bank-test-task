package com.example.messagingapp.controller;

import com.example.messagingapp.dto.MessageRequest;
import com.example.messagingapp.dto.MessageResponse;
import com.example.messagingapp.entity.MessageStatus;
import com.example.messagingapp.service.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

    @Mock
    private MessageService messageService;

    @InjectMocks
    private MessageController messageController;

    @Test
    @DisplayName(value = "saveMessageToDB")
    void saveMessage() {
        UUID randomUUID = UUID.randomUUID();
        MessageRequest messageRequest = MessageRequest.builder()
                .id(randomUUID)
                .content("Новое сообщение")
                .timestamp(Instant.now())
                .build();
        MessageResponse expectedResponse = MessageResponse.builder()
                .messageId(randomUUID)
                .status(MessageStatus.PROCESSED)
                .processedAt(Instant.now())
                .build();
        Mockito.when(messageService.saveMessage(messageRequest)).thenReturn(expectedResponse);
        MessageResponse actualResponse = messageService.saveMessage(messageRequest);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getMessageId(),actualResponse.getMessageId());
        assertEquals(expectedResponse.getStatus(),actualResponse.getStatus());
        assertEquals(expectedResponse.getProcessedAt(),actualResponse.getProcessedAt());

        Mockito.verify(messageService,Mockito.times(1)).saveMessage(messageRequest);
    }

    @Test
    void getAllMessages() {



    }

    @Test
    void getMessageByID() {

    }
}