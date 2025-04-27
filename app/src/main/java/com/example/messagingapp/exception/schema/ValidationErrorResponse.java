package com.example.messagingapp.exception.schema;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "Ошибка валидации")
public class ValidationErrorResponse {

    @Schema(description = "Сообщение об ошибке", example = "Validation failed")
    private String message;

    @Schema(description = "Время получения ошибки валидации", example = "2023-11-15T14:30:00Z")
    private Instant timestamp;

    @Schema(description = "Список ошибок")
    private List<FieldMessageError> errors;
}
