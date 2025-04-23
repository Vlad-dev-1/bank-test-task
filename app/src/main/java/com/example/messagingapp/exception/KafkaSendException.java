package com.example.messagingapp.exception;

import lombok.Getter;

@Getter
public class KafkaSendException extends RuntimeException{

    public static final String FAILED_SEND_KAFKA = "Не удалось отправить сообщение в Kafka";

    public KafkaSendException(String message) {
        super(message);
    }
}
