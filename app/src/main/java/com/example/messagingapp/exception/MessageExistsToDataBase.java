package com.example.messagingapp.exception;

import lombok.Getter;

@Getter
public class MessageExistsToDataBase extends RuntimeException{

    public static final String MESSAGE_EXISTS_TO_DB = "Сообщение с таким ID уже обработано";

    public MessageExistsToDataBase(String message) {
        super(message);
    }
}
