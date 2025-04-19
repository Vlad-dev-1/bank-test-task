package com.example.messagingapp.service;


import com.example.messagingapp.dto.MessageRequestDto;
import com.example.messagingapp.dto.MessageResponseDto;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    MessageResponseDto saveMessage(MessageRequestDto messageRequestDto);

    List<MessageResponseDto> getMessages();

    MessageResponseDto getMessageByID(UUID idMessage);
}
