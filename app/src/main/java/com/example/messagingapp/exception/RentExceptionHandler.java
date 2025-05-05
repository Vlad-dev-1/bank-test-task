package com.example.messagingapp.exception;

import com.example.messagingapp.exception.schema.ValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.messagingapp.exception.schema.FieldMessageError;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;


@RestControllerAdvice
public class RentExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException e) {

        List<FieldMessageError> errors = e.getBindingResult().getAllErrors().stream()
                .map(error -> new FieldMessageError(((FieldError) error).getField(),
                        error.getDefaultMessage()))
                .collect(Collectors.toList());

        ValidationErrorResponse response = new ValidationErrorResponse(
                "Validation failed",
                Instant.now(),
                errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MessageExistsToDataBase.class)
    public ResponseEntity<String> handleMessageExistsToDataBaseException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler({MessagesNotFound.class,
            MessageNotFoundById.class,
            KafkaSendException.class,
            MessageStatisticIsEmpty.class,
            MessagesGetException.class,
            MessageGetByIDException.class,
            MessageStatisticException.class})
    public ResponseEntity<ErrorResponceApp> handleNotFoundException(RuntimeException e) {
        return buildErrorResponseEntity(e.getMessage());
    }

    private ResponseEntity<ErrorResponceApp> buildErrorResponseEntity(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponceApp(message));
    }
}
