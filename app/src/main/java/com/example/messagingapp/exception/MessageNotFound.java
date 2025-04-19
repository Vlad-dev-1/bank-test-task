package com.example.messagingapp.exception;

import lombok.Getter;

@Getter
public class MessageNotFound extends RuntimeException{

    public static final String MESSAGES_NOT_FOUND = "Сообщения не найдены в базе данных";

    public MessageNotFound(String message) {
        super(message);
    }
}
