package com.example.messagingapp.exception;

import lombok.Getter;

@Getter
public class MessageStatisticException extends RuntimeException {

    public static final String MESSAGE_STATISTIC_EXCEPTION = "Ошибка при получении статистики сообщений";

    public MessageStatisticException(String message) {
        super(message);
    }
}
