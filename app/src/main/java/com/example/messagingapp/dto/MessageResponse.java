package com.example.messagingapp.dto;

import com.example.messagingapp.entity.MessageStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class MessageResponse {

    private UUID messageId;

    private MessageStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant processedAt;
}
