package com.example.messagingapp.kafka.consumer;

import com.example.messagingapp.dto.MessageRequest;
import com.example.messagingapp.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageConsumer {

    @KafkaListener(
            topics = "${app.kafka.input-topic}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consume(@Payload MessageRequest message,
                        @Header(KafkaHeaders.RECEIVED_KEY) String key,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            log.info("Получено сообщение типа {} из топика {}: {}", key, topic, message);
        } catch (Exception e) {
            log.error("Ошибка получения сообщения MessageRequest");
        }
    }

    @KafkaListener(
            topics = "${app.kafka.output-topic}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consume(@Payload MessageResponse message,
                        @Header(KafkaHeaders.RECEIVED_KEY) String key,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            log.info("Получено сообщение типа {} из топика {}: {}", key, topic, message);
        } catch (Exception e) {
            log.error("Ошибка получения сообщения MessageResponse");
        }
    }
}
