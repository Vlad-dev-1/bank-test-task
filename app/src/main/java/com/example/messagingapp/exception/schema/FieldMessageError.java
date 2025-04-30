package com.example.messagingapp.exception.schema;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Ошибка поля")
public class FieldMessageError {

    @Schema(description = "Имя поля", example = "ID")
    private String field;

    @Schema(description = "Сообщение об ошибке", example = "ID сообщения не может быть null")
    private String message;
}
