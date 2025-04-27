package com.example.messagingapp.dto;

import com.example.messagingapp.entity.MessageStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Ответ с данными сообщения")
public class MessageResponse {

    @Schema(description = "Идентификатор сообщения",
            example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID messageId;

    @Schema(description = "Статус сообщения",
            example = "PROCESSED")
    private MessageStatus status;

    @Schema(description = "Временная метка обработки",
            example = "2023-10-25T12:00:00Z",
            pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant processedAt;
}
