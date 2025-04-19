package com.example.messagingapp.exception;

import lombok.Getter;

@Getter
public class MessageNotFoundById extends RuntimeException {

    public static final String MESSAGE_NOT_FOUND_BY_ID = "Не найдено сообщение по ID";

    public MessageNotFoundById(String message) {
        super(message);
    }
}
