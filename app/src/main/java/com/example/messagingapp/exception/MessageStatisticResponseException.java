package com.example.messagingapp.exception;

import lombok.Getter;

@Getter
public class MessageStatisticResponseException extends RuntimeException{

    public static final String MESSAGE_STATISTIC_EXCEPTION = "Произошла ошибка при получении статистики сообщений";

    public MessageStatisticResponseException(String message) {
        super(message);
    }
}
