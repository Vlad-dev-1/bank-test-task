package com.example.messagingapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RentExceptionHandler {

    @ExceptionHandler({MessagesNotFound.class,
            MessageNotFoundById.class,
            KafkaSendException.class,
            MessageStatisticResponseException.class})
    public ResponseEntity<ErrorResponceApp> handleNotFoundException(RuntimeException e) {
        return buildErrorResponseEntity(e.getMessage());
    }

    private ResponseEntity<ErrorResponceApp> buildErrorResponseEntity(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponceApp(message));
    }
}
