package com.example.messagingapp.exception;

import lombok.Getter;

@Getter
public class MessagesGetException extends RuntimeException {

    public static final String MESSAGES_GET_EXCEPTION = "Ошибка получения сообщений";

    public MessagesGetException(String message) {
        super(message);
    }
}
