package com.example.messagingapp.controller;


import com.example.messagingapp.dto.MessageStatisticResponse;
import com.example.messagingapp.service.MessageStatisticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statistics")
@Tag(name = "Контроллер статистики сообщений в базе данных",
        description = "API для операций статистики сообщений в базе данных по статусу")
public class MessageStatisticController {

    private final MessageStatisticService messageStatisticService;

    @Operation(summary = "Получить статистику сообщений в базе данных",
            description = "Возвращает список всех сообщений с группированных по статусу")
    @ApiResponse(
            responseCode = "200",
            description = "Успешный ответ",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageStatisticResponse.class),
                    examples = @ExampleObject(
                            value = "{\"PROCESSED\": 23," +
                                    "\"FAILED\": 2}",
                            description = "Пример ответа"
                    )
            )
    )
    @GetMapping
    public MessageStatisticResponse getMessagesStatistic() {
        log.info("Запрос статистики сообщений по статусам");
        return messageStatisticService.getMessageStatistic();
    }
}
