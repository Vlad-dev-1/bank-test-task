package com.example.messagingapp.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RentExceptionHandler {

    @ExceptionHandler(MessageNotFound.class)
    public ResponseEntity<?> catchException(MessageNotFound e){
        return buildErrorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MessageNotFoundById.class)
    public ResponseEntity<?> catchException(MessageNotFoundById e){
        return buildErrorResponseEntity(e.getMessage(),HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<?> buildErrorResponseEntity(String message, HttpStatus errorStatus){
        return new ResponseEntity<>(message, errorStatus);
    }

}
