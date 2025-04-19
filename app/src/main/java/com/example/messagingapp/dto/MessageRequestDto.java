package com.example.messagingapp.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class MessageRequestDto {

    private UUID id;

    private String content;

    private OffsetDateTime timestamp;
}
