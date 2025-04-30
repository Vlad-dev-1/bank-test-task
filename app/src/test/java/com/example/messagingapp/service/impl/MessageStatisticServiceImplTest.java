package com.example.messagingapp.service.impl;

import com.example.messagingapp.dto.MessageStatisticResponse;
import com.example.messagingapp.entity.Message;
import com.example.messagingapp.entity.MessageStatus;
import com.example.messagingapp.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageStatisticServiceImplTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private Resilience4JCircuitBreakerFactory circuitBreakerFactory;

    @Mock
    private CircuitBreaker circuitBreaker;

    @InjectMocks
    private MessageStatisticServiceImpl messageStatisticService;

    @Test
    void getMessageStatistic_ShouldReturnStatistics() {

        Message message1 = new Message();
        message1.setStatus(MessageStatus.PROCESSED);

        Message message2 = new Message();
        message2.setStatus(MessageStatus.FAILED);

        Message message3 = new Message();
        message3.setStatus(MessageStatus.PROCESSED);

        when(messageRepository.findAll()).thenReturn(List.of(message1, message2, message3));
        when(circuitBreakerFactory.create("messageStatisticService")).thenReturn(circuitBreaker);
        when(circuitBreaker.run(any(), any())).thenAnswer(invocation -> {
            // Получаем и выполняем supplier из первого аргумента
            return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
        });


        MessageStatisticResponse result = messageStatisticService.getMessageStatistic();

        assertEquals(2, result.getStatisticMessages().get("PROCESSED"));
        assertEquals(1, result.getStatisticMessages().get("FAILED"));
        verify(messageRepository,times(1)).findAll();
    }
}