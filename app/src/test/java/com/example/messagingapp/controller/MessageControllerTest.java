package com.example.messagingapp.controller;

import com.example.messagingapp.dto.MessageRequest;
import com.example.messagingapp.dto.MessageResponse;
import com.example.messagingapp.entity.MessageStatus;
import com.example.messagingapp.service.MessageService;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

    @Mock
    private MessageService messageService;

    @InjectMocks
    private MessageController messageController;

    @Test
    void saveMessage_ShouldReturnMessageResponse() {

        MessageRequest request = MessageRequest.builder()
                .id(UUID.randomUUID())
                .content("Текст сообщения")
                .timestamp(Instant.now())
                .build();

        MessageResponse expectedResponse = MessageResponse.builder()
                .messageId(request.getId())
                .status(MessageStatus.PROCESSED)
                .processedAt(Instant.now())
                .build();
        when(messageService.saveMessage(request)).thenReturn(expectedResponse);

        MessageResponse actualResponse = messageController.saveMessage(request);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getMessageId(), actualResponse.getMessageId());
        assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
        verify(messageService, times(1)).saveMessage(request);
    }

    @Test
    void getAllMessages_ShouldReturnMessageResponseList() {

        MessageResponse message1 = MessageResponse.builder()
                .messageId(UUID.randomUUID())
                .status(MessageStatus.PROCESSED)
                .processedAt(Instant.now())
                .build();
        MessageResponse message2 = MessageResponse.builder()
                .messageId(UUID.randomUUID())
                .status(MessageStatus.FAILED)
                .processedAt(Instant.now())
                .build();
        List<MessageResponse> expectedMessages = List.of(message1, message2);
        when(messageService.getMessages()).thenReturn(expectedMessages);

        List<MessageResponse> actualMessages = messageController.getAllMessages();

        assertEquals(2, actualMessages.size());
        assertTrue(actualMessages.containsAll(expectedMessages));
        verify(messageService, times(1)).getMessages();
    }

    @Test
    void getMessageById_ShouldReturnMessageResponseById() {

        UUID messageId = UUID.randomUUID();
        MessageResponse expectedResponse = MessageResponse.builder()
                .messageId(messageId)
                .status(MessageStatus.PROCESSED)
                .processedAt(Instant.now())
                .build();
        when(messageService.getMessageByID(messageId)).thenReturn(expectedResponse);

        MessageResponse actualResponse = messageController.getMessageByID(messageId);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getMessageId(), actualResponse.getMessageId());
        verify(messageService, times(1)).getMessageByID(messageId);
    }
}