package com.example.messagingapp.dto;

import com.example.messagingapp.entity.MessageStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class MessageResponseDto {

    private UUID messageId;

    private MessageStatus status;

    private OffsetDateTime processedAt;
}
