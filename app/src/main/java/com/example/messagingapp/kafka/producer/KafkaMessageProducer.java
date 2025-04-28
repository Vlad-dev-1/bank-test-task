package com.example.messagingapp.kafka.producer;


import com.example.messagingapp.dto.MessageRequest;
import com.example.messagingapp.dto.MessageResponse;

import com.example.messagingapp.exception.KafkaSendException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static com.example.messagingapp.exception.KafkaSendException.FAILED_SEND_KAFKA;


@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageProducer {

    private static final String SEND_SUCCESS_MESSAGE_TOPIC = "Сообщение отправлено. Топик: {}," +
            " Ключ: {}, Партиция: {}, Оффсет: {}";
    private static final String SEND_ERROR_MESSAGE_TOPIC = "Ошибка отправки сообщения. Топик: {}, Ключ: {}";

    @Value("${app.kafka.input-topic}")
    private String inputTopic;

    @Value("${app.kafka.output-topic}")
    private String outputTopic;

    private final KafkaTemplate<String, Object> requestMessageKafkaTemplate;
    private final KafkaTemplate<String, Object> responseMessageKafkaTemplate;
    private final Resilience4JCircuitBreakerFactory circuitBreakerFactory;

    public CompletableFuture<Void> sendMessageRequest(MessageRequest messageRequest) {
        String key = messageRequest.getId().toString();
        return sendMessageToPartition(requestMessageKafkaTemplate, inputTopic, key, messageRequest);
    }

    public CompletableFuture<Void> sendMessageResponse(MessageResponse messageResponse) {
        String key = messageResponse.getMessageId().toString();
        return sendMessageToPartition(responseMessageKafkaTemplate, outputTopic, key, messageResponse);
    }

    private CompletableFuture<Void> sendMessageToPartition(KafkaTemplate<String, Object> kafkaTemplate,
                                                           String topic,
                                                           String key,
                                                           Object message) {

        // Выполнение операции с Circuit Breaker
        return circuitBreakerFactory.create("kafkaProducer").run(() ->
                kafkaTemplate.send(topic, key, message)
                        .thenApply(result -> {
                            log.info(SEND_SUCCESS_MESSAGE_TOPIC,
                                    topic,
                                    key,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                            return null;
                        }), ex -> {
            log.error(SEND_ERROR_MESSAGE_TOPIC, topic, key, ex);
            throw new KafkaSendException(FAILED_SEND_KAFKA);
        });
    }
}
