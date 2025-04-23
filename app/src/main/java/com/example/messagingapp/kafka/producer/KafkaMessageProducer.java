package com.example.messagingapp.kafka.producer;


import com.example.messagingapp.dto.MessageRequest;
import com.example.messagingapp.dto.MessageResponse;

import com.example.messagingapp.exception.KafkaSendException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static com.example.messagingapp.exception.KafkaSendException.FAILED_SEND_KAFKA;


@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageProducer {

    private static final String SEND_SUCCESS_MESSAGE_TOPIC = "Сообщение отправлено. Топик: {}, Ключ: {}, Партиция: {}, Оффсет: {}";
    private static final String SEND_ERROR_MESSAGE_TOPIC = "Ошибка отправки сообщения. Топик: {}, Ключ: {}";

    @Value("${app.kafka.input-topic}")
    private String inputTopic;

    @Value("${app.kafka.output-topic}")
    private String outputTopic;

    private final KafkaTemplate<String, Object> requestMessageKafkaTemplate;
    private final KafkaTemplate<String, Object> responseMessageKafkaTemplate;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @PostConstruct
    public void init() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("kafkaProducer");
        circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                        log.warn("Circuit Breaker изменил состояние: {}", event.getStateTransition()));
    }

    public CompletableFuture<Void> sendMessageRequest(MessageRequest messageRequest) {
        String key = messageRequest.getId().toString();
        return sendMessage(requestMessageKafkaTemplate, inputTopic, key, messageRequest);
    }

    public CompletableFuture<Void> sendMessageResponse(MessageResponse messageResponse) {
        String key = messageResponse.getMessageId().toString();
        return sendMessage(responseMessageKafkaTemplate, outputTopic, key, messageResponse);
    }

    private CompletableFuture<Void> sendMessage(KafkaTemplate<String, Object> kafkaTemplate,
                                                String topic,
                                                String key,
                                                Object message) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("kafkaProducer");

        return circuitBreaker.executeSupplier(() ->
                kafkaTemplate.send(topic, key, message)
                        .thenApply(result -> {
                            log.info(SEND_SUCCESS_MESSAGE_TOPIC,
                                    topic,
                                    key,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                            return (Void) null;
                        })
                        .exceptionally(ex -> {
                            log.error(SEND_ERROR_MESSAGE_TOPIC, topic, key, ex);
                            throw new KafkaSendException(FAILED_SEND_KAFKA);
                        })
        );
    }
}
