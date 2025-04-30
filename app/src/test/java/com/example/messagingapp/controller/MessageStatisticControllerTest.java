package com.example.messagingapp.controller;

import com.example.messagingapp.dto.MessageStatisticResponse;
import com.example.messagingapp.service.MessageStatisticService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageStatisticControllerTest {

    @Mock
    private MessageStatisticService messageStatisticService;

    @InjectMocks
    private MessageStatisticController messageStatisticController;


    @Test
    void getMessagesStatistic_ShouldReturnMessageStatisticResponse() {

        Map<String, Long> expectedStatistic = Map.of(
                "PROCESSED", 23L,
                "FAILED", 2L
        );
        MessageStatisticResponse expectedResponse = new MessageStatisticResponse(expectedStatistic);

        when(messageStatisticService.getMessageStatistic()).thenReturn(expectedResponse);

        MessageStatisticResponse actualResponse = messageStatisticController.getMessagesStatistic();

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        assertEquals(23L, actualResponse.getStatisticMessages().get("PROCESSED"));
        assertEquals(2L, actualResponse.getStatisticMessages().get("FAILED"));
        verify(messageStatisticService, times(1)).getMessageStatistic();
    }
}