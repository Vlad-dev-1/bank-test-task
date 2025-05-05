package com.example.messagingapp.service.impl;

import com.example.messagingapp.dto.MessageRequest;
import com.example.messagingapp.dto.MessageResponse;
import com.example.messagingapp.entity.Message;
import com.example.messagingapp.entity.MessageStatus;
import com.example.messagingapp.exception.MessageExistsToDataBase;
import com.example.messagingapp.exception.MessageNotFoundById;
import com.example.messagingapp.exception.MessagesNotFound;
import com.example.messagingapp.kafka.producer.KafkaMessageProducer;
import com.example.messagingapp.mapper.MapperMessage;
import com.example.messagingapp.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MapperMessage mapperMessage;

    @Mock
    private KafkaMessageProducer kafkaMessageProducer;

    @Mock
    private Resilience4JCircuitBreakerFactory circuitBreakerFactory;

    @Mock
    private CircuitBreaker circuitBreaker;

    @InjectMocks
    private MessageServiceImpl messageService;

    @BeforeEach
    void setUp() {
        when(circuitBreakerFactory.create(anyString())).thenReturn(circuitBreaker);
    }

    @Test
    void saveMessage_ShouldSaveNewMessage() {

        MessageRequest request = new MessageRequest();
        request.setId(UUID.randomUUID());

        Message message = new Message();
        MessageResponse response = new MessageResponse();

        when(messageRepository.findMessageById(request.getId())).thenReturn(Optional.empty());
        when(messageRepository.save(message)).thenReturn(message);
        when(mapperMessage.mapperMessageRequestToMessage(request)).thenReturn(message);
        when(mapperMessage.mapperMessageToMessageResponse(message)).thenReturn(response);

        when(circuitBreaker.run(any(Supplier.class), any(Function.class)))
                .thenAnswer(invocation -> {
                    Supplier<MessageResponse> supplier = invocation.getArgument(0);
                    return supplier.get();
                });

        MessageResponse actualMessageResponse = messageService.saveMessage(request);

        assertEquals(response, actualMessageResponse);
        verify(messageRepository,times(1)).save(message);
        verify(mapperMessage,times(1)).mapperMessageRequestToMessage(request);
        verify(mapperMessage,times(1)).mapperMessageToMessageResponse(message);
        verify(kafkaMessageProducer,times(1)).sendMessageRequest(request);
        verify(kafkaMessageProducer,times(1)).sendMessageResponse(response);

    }

    @Test
    void saveMessage_ShouldThrowWhenMessageIsPresent() {

        UUID id = UUID.randomUUID();

        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setId(id);

        Message message = new Message();
        message.setId(id);
        message.setStatus(MessageStatus.PROCESSED);

        when(messageRepository.findMessageById(id)).thenReturn(Optional.of(message));
        when(circuitBreaker.run(any(Supplier.class), any(Function.class)))
                .thenAnswer(invocation -> {
                    Supplier<MessageResponse> supplier = invocation.getArgument(0);
                    return supplier.get();
                });

        assertThrows(MessageExistsToDataBase.class, ()-> messageService.saveMessage(messageRequest));
        verify(messageRepository,times(1)).findMessageById(id);
    }

    @Test
    void getMessages_ShouldReturnMessages() {

        List<Message> messages = List.of(new Message());
        List<MessageResponse> expected = List.of(new MessageResponse());

        when(messageRepository.findAll()).thenReturn(messages);
        when(mapperMessage.getAllMessageResponse(messages)).thenReturn(expected);

        when(circuitBreaker.run(any(Supplier.class), any(Function.class)))
                .thenAnswer(invocation -> {
                    Supplier<List<MessageResponse>> supplier = invocation.getArgument(0);
                    return supplier.get();
                });

        List<MessageResponse> result = messageService.getMessages();

        assertEquals(expected, result);
        verify(messageRepository,times(1)).findAll();
        verify(mapperMessage,times(1)).getAllMessageResponse(messages);

    }


    @Test
    void getMessages_ShouldThrowWhenEmpty() {

        when(messageRepository.findAll()).thenReturn(Collections.emptyList());

        when(circuitBreaker.run(any(Supplier.class), any(Function.class)))
                .thenAnswer(invocation -> {
                    Supplier<List<MessageResponse>> supplier = invocation.getArgument(0);
                    return supplier.get(); // выбросит исключение
                });

        assertThrows(MessagesNotFound.class, () -> messageService.getMessages());
        verify(messageRepository,times(1)).findAll();
    }

    @Test
    void getMessageByID_ShouldReturnMessage() {

        UUID id = UUID.randomUUID();
        Message message = new Message();
        MessageResponse expected = new MessageResponse();

        when(messageRepository.findById(id)).thenReturn(Optional.of(message));
        when(mapperMessage.mapperMessageToMessageResponse(message)).thenReturn(expected);

        when(circuitBreaker.run(any(Supplier.class), any(Function.class)))
                .thenAnswer(invocation -> {
                    Supplier<MessageResponse> supplier = invocation.getArgument(0);
                    return supplier.get();
                });

        MessageResponse result = messageService.getMessageByID(id);

        assertEquals(expected, result);
        verify(messageRepository,times(1)).findById(id);
        verify(mapperMessage,times(1)).mapperMessageToMessageResponse(message);
    }

    @Test
    void getMessageByID_ShouldThrowWhenNotFound() {

        UUID id = UUID.randomUUID();

        when(messageRepository.findById(id)).thenReturn(Optional.empty());

        when(circuitBreaker.run(any(Supplier.class), any(Function.class)))
                .thenAnswer(invocation -> {
                    Supplier<MessageResponse> supplier = invocation.getArgument(0);
                    return supplier.get(); // выбросит исключение
                });

        assertThrows(MessageNotFoundById.class, () -> messageService.getMessageByID(id));
        verify(messageRepository,times(1)).findById(id);
    }
}