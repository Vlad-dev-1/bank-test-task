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

    @KafkaListener(
            topics = "${app.kafka.input-topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            properties = "metadata.max.age.ms=10000"  // Чаще обновлять метаданные о брокерах
    )
    @Retryable(
            retryFor = KafkaException.class,
            maxAttemptsExpression = "#{${spring.kafka.listener.retry.max-attempts}}",
            backoff = @Backoff(delayExpression = "#{${spring.kafka.listener.retry.interval}}")
    )
    public void consumeInputTopic(@Payload MessageRequest message,
                                  @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        processMessage(message, key, topic, partition, "MessageRequest");
    }

    @KafkaListener(
            topics = "${app.kafka.output-topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            properties = "metadata.max.age.ms=10000"  // Чаще обновлять метаданные о брокерах
    )
    @Retryable(
            retryFor = KafkaException.class,
            maxAttemptsExpression = "#{${spring.kafka.listener.retry.max-attempts}}",
            backoff = @Backoff(delayExpression = "#{${spring.kafka.listener.retry.interval}}")
    )
    public void consumeOutputTopic(@Payload MessageResponse message,
                                   @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        processMessage(message, key, topic, partition, "MessageResponse");
    }

    public  <T> void processMessage(T message, String key, String topic, int partition, String messageType) {
        try {
            if (message == null) {
                log.warn("Получено пустое сообщение (ключ: {}) типа {} из топика {}, партиция {}",
                        key, messageType, topic, partition);
                return;
            }
            log.info("Получено сообщение (ключ: {}) типа {} из топика {}, партиция {}: {}",
                    key, messageType, topic, partition, message);

        } catch (SerializationException e) {
            log.error("Ошибка десериализации {} (партиция {}): {}", messageType, partition, e.getMessage());
        } catch (KafkaException e) {
            log.error("Ошибка Kafka при обработке {} (партиция {}): {}", messageType, partition, e.getMessage());
        } catch (Exception e) {
            log.error("Неожиданная ошибка при обработке {} (партиция {}): {}", messageType, partition, e.getMessage(), e);
        }
    }
}
