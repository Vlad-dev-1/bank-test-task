package com.example.messagingapp.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API для обработки сообщений",
                version = "1.0.0",
                description = "API для управления сообщениями"
        )
)
public class ConfigOpenApi {

}
