package com.example.messagingapp.kafka.producer;


import com.example.messagingapp.dto.MessageRequest;
import com.example.messagingapp.dto.MessageResponse;

import com.example.messagingapp.exception.KafkaSendException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static com.example.messagingapp.exception.KafkaSendException.FAILED_SEND_KAFKA;


@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageProducer {

    private static final String SEND_SUCCESS_MESSAGE_TOPIC = "Сообщение успешно отправлено." +
            " Топик: {}, Ключ: {}, Партиция: {}, Оффсет: {}";

    private static final String SEND_ERROR_FORMAT = "Сработал Circuit Breaker при отправке в Kafka." +
            " Ошибка отправки сообщения в Kafka Топик: {}, Ключ: {}. Причина: {}";

    @Value("${spring.kafka.topics.inputTopic.name}")
    private String inputTopic;

    @Value("${spring.kafka.topics.outputTopic.name}")
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

        return circuitBreakerFactory.create("kafkaProducer").run(() ->
                {
                    CompletableFuture<SendResult<String, Object>> sendResultCompletableFuture = kafkaTemplate
                            .send(topic, 0, key, message);
                    return sendResultCompletableFuture
                            .thenAccept(result -> log.info(SEND_SUCCESS_MESSAGE_TOPIC, topic, key,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset()));
                },
                exception -> {
                    log.error(SEND_ERROR_FORMAT, topic, key, exception.getMessage(), exception);
                    throw new KafkaSendException(FAILED_SEND_KAFKA);
                });
    }
}
