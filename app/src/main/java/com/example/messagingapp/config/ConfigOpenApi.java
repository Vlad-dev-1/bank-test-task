package com.example.messagingapp.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API для обработки сообщений",
                version = "1.0.0",
                description = "API для управления сообщениями"
        )
)
public class ConfigOpenApi {

    public ConfigOpenApi() {
        log.info("Инициализация конфигурации OpenAPI для API обработки сообщений");
    }
}
