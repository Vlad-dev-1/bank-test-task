package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableRetry
public class MessageAppApplication {
    public static void main(String[] args) {

        SpringApplication.run(MessageAppApplication.class, args);
    }
    @RestController
    public static class HelloController {
        @GetMapping("/")
        public String greeting() {
            return "Hello World!";
        }
    }
}
