package com.example.messagingapp.service;


import com.example.messagingapp.dto.MessageRequest;
import com.example.messagingapp.dto.MessageResponse;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    MessageResponse saveMessage(MessageRequest messageRequest);

    List<MessageResponse> getMessages();

    MessageResponse getMessageByID(UUID idMessage);
}
