package com.example.messagingapp.kafka.consumer;

import com.example.messagingapp.dto.MessageRequest;
import com.example.messagingapp.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageConsumer {

    @KafkaListener(topics = "${app.kafka.input-topic}", groupId = "${spring.kafka.consumer.group-id}")
    @Retryable(
            retryFor = KafkaException.class,
            maxAttemptsExpression = "#{${spring.kafka.listener.retry.max-attempts}}",
            backoff = @Backoff(delayExpression = "#{${spring.kafka.listener.retry.interval}}"))
    public void consume(@Payload MessageRequest message,
                        @Header(KafkaHeaders.RECEIVED_KEY) String key,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        processMessage(message, key, topic, "MessageRequest");
    }

    @KafkaListener(topics = "${app.kafka.output-topic}", groupId = "${spring.kafka.consumer.group-id}")
    @Retryable(
            retryFor = KafkaException.class,
            maxAttemptsExpression = "#{${spring.kafka.listener.retry.max-attempts}}",
            backoff = @Backoff(delayExpression = "#{${spring.kafka.listener.retry.interval}}"))
    public void consume(@Payload MessageResponse message,
                        @Header(KafkaHeaders.RECEIVED_KEY) String key,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        processMessage(message, key, topic, "MessageResponse");
    }

    private <T> void processMessage(T message, String key, String topic, String messageType) {
        try {
            if (message == null) {
                log.warn("Получено пустое сообщение типа {} из топика {}", messageType, topic);
                return;
            }
            log.info("Получено сообщение типа {} из топика {}: {}", key, topic, message);

        } catch (SerializationException e) {
            log.error("Ошибка десериализации {}: {}", messageType, e.getMessage());
        } catch (KafkaException e) {
            log.error("Ошибка Kafka при обработке {}: {}", messageType, e.getMessage());
        } catch (Exception e) {
            log.error("Неожиданная ошибка при обработке {}: {}", messageType, e.getMessage(), e);
        }
    }
}
