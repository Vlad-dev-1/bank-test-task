package com.example.messagingapp.exception;


import lombok.Getter;

@Getter
public class MessagesNotFound extends RuntimeException{

    public static final String MESSAGES_NOT_FOUND = "Сообщения не найдены в базе данных";

    public MessagesNotFound(String message) {
        super(message);
    }
}
