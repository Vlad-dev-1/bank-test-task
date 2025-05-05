package com.example.messagingapp.exception;

import lombok.Getter;

@Getter
public class MessageGetByIDException extends RuntimeException {

    public static final String MESSAGE_GET_BY_ID_EXCEPTION = "Ошибка получения сообщения по ID";

    public MessageGetByIDException(String message) {
        super(message);
    }
}
