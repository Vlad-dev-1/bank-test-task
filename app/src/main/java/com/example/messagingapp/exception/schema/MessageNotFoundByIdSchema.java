package com.example.messagingapp.exception.schema;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Ответ с ошибкой")
public class MessageNotFoundByIdSchema {

    @Schema(description = "Сообщение об ошибке",
            example = "Не найдено сообщение по ID")
    private String messageError;
}
