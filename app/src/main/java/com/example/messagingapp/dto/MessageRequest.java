package com.example.messagingapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Запрос для создания сообщения")
public class MessageRequest {

    @Schema(description = "Уникальный идентификатор сообщения",
            example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Текст сообщения",
            example = "Пример текста сообщения")
    private String content;

    @Schema(description = "Временная метка создания",
            example = "2023-10-25T12:00:00Z",
            pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant timestamp;
}
