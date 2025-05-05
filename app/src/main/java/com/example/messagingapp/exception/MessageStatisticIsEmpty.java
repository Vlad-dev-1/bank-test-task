package com.example.messagingapp.exception;

import lombok.Getter;

@Getter
public class MessageStatisticIsEmpty extends RuntimeException {

    public static final String MESSAGE_STATISTIC_IS_EMPTY = "Статистика сообщений не найдена";

    public MessageStatisticIsEmpty(String message) {
        super(message);
    }
}
