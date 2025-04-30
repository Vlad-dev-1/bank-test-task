package com.example.messagingapp.controller;

import com.example.messagingapp.dto.MessageRequest;
import com.example.messagingapp.dto.MessageResponse;
import com.example.messagingapp.exception.schema.MessageNotFoundByIdSchema;
import com.example.messagingapp.exception.schema.MessagesNotFoundSchema;
import com.example.messagingapp.exception.schema.ValidationErrorResponse;
import com.example.messagingapp.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
@Validated
@Tag(name = "Контроллер сообщений", description = "API для операций с сообщениями")
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "Обработать новое сообщение", description = "Обрабатывает и сохраняет новое сообщение")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Сообщение успешно обработано",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невалидные данные запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ValidationErrorResponse.class)))})
    @PostMapping
    public MessageResponse saveMessage(@RequestBody
                                       @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                               description = "Данные для создания сообщения",
                                               required = true,
                                               content = @Content(
                                                       mediaType = "application/json",
                                                       schema = @Schema(implementation = MessageRequest.class)))
                                       @Valid MessageRequest messageRequest) {

        log.info("Попытка обработки нового сообщения");
        return messageService.saveMessage(messageRequest);
    }

    @Operation(summary = "Получить все сообщения", description = "Возвращает список всех сообщений")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = MessageResponse.class))))
            ,
            @ApiResponse(
                    responseCode = "404",
                    description = "Нет содержимого в базе данных",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessagesNotFoundSchema.class)))})
    @GetMapping
    public List<MessageResponse> getAllMessages() {
        log.info("Запрос на получение всех сообщений");
        return messageService.getMessages();
    }

    @Operation(summary = "Получить сообщение по ID",
            description = "Возвращает конкретное сообщение по его идентификатору")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Сообщение найдено",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)))
            ,
            @ApiResponse(
                    responseCode = "400",
                    description = "Невалидный ID сообщения",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ValidationErrorResponse.class)))
            ,
            @ApiResponse(
                    responseCode = "404",
                    description = "Сообщение не найдено",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageNotFoundByIdSchema.class)))})
    @GetMapping("/{id}")
    public MessageResponse getMessageByID(@Parameter(
            description = "ID сообщения для поиска",
            required = true,
            example = "550e8400-e29b-41d4-a716-446655440000")
                                          @PathVariable("id")
                                          @NotNull(message = "ID сообщения не может быть null") UUID messageID) {
        log.info("Поиск сообщения по ID: {}", messageID);
        return messageService.getMessageByID(messageID);
    }
}
