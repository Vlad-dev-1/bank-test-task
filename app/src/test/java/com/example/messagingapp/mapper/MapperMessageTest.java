package com.example.messagingapp.mapper;

import com.example.messagingapp.dto.MessageRequest;
import com.example.messagingapp.dto.MessageResponse;
import com.example.messagingapp.entity.Message;
import com.example.messagingapp.entity.MessageStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MapperMessageTest {

    @Spy
    @InjectMocks
    private MapperMessageImpl mapperMessage; // Используем реализацию, сгенерированную MapStruct

    private final UUID testId = UUID.randomUUID();
    private final Instant testTimestamp = Instant.now();
    private final Instant testProcessedAt = Instant.now().plusSeconds(15L);
    private final MessageStatus testMessageStatus = MessageStatus.PROCESSED;
    private final String testContent = "Тест контент";

    @Test
    void mapperMessageRequestToMessage_ShouldMapCorrectly() {

        MessageRequest request = MessageRequest.builder()
                .id(testId)
                .content(testContent)
                .timestamp(testTimestamp)
                .build();

        Message result = mapperMessage.mapperMessageRequestToMessage(request);

        assertNotNull(result);
        assertEquals(request.getId(), result.getId());
        assertEquals(request.getContent(), result.getContent());
        assertEquals(request.getTimestamp(), result.getTimestamp());
        assertNull(result.getStatus());
        assertNull(result.getProcessedAt());
        verify(mapperMessage,times(1)).mapperMessageRequestToMessage(request);
    }

    @Test
    void mapperMessageToMessageResponse_ShouldMapCorrectly() {

        Message message = Message.builder()
                .id(testId)
                .content(testContent)
                .status(testMessageStatus)
                .timestamp(testTimestamp)
                .processedAt(testProcessedAt)
                .build();

        MessageResponse result = mapperMessage.mapperMessageToMessageResponse(message);

        assertNotNull(result);
        assertEquals(message.getId(), result.getMessageId());
        assertEquals(message.getStatus(), result.getStatus());
        assertEquals(message.getProcessedAt(), result.getProcessedAt());
        verify(mapperMessage,times(1)).mapperMessageToMessageResponse(message);
    }

    @Test
    void getAllMessageResponseResponse_ShouldReturnEmptyListForNullInput() {

        List<MessageResponse> result = mapperMessage.getAllMessageResponse(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mapperMessage, times(1)).getAllMessageResponse(null);
    }

    @Test
    void getAllMessageResponseResponse_ShouldMapListCorrectly() {

        Message message1 = Message.builder()
                .id(testId)
                .content(testContent)
                .status(testMessageStatus)
                .timestamp(testTimestamp)
                .processedAt(testProcessedAt)
                .build();

        Message message2 = Message.builder()
                .id(UUID.randomUUID())
                .content("Другой контент")
                .status(MessageStatus.FAILED)
                .timestamp(testTimestamp.plusSeconds(10))
                .processedAt(testProcessedAt.plusSeconds(15))
                .build();

        List<Message> messages = List.of(message1, message2);

        List<MessageResponse> result = mapperMessage.getAllMessageResponse(messages);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(message1.getId(), result.get(0).getMessageId());
        assertEquals(message1.getStatus(), result.get(0).getStatus());
        assertEquals(message2.getId(), result.get(1).getMessageId());
        assertEquals(message2.getStatus(), result.get(1).getStatus());
        assertEquals(message2.getProcessedAt(),result.get(1).getProcessedAt());
        verify(mapperMessage, times(1)).getAllMessageResponse(messages);
    }
}