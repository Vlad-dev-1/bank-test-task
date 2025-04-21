package com.example.messagingapp.config;


import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@EnableKafka
@Configuration
public class ConfigKafka {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.input-topic}")
    private String inputTopic;

    @Value("${app.kafka.output-topic}")
    private String outputTopic;

    @Value("${spring.kafka.consumer.group-id}")
    private String kafkaGroupID;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    @Value("${spring.kafka.listener.retry.interval}")
    private Long interval;

    @Value("${spring.kafka.listener.retry.max-attempts}")
    private Long maxAttempts;

    // Producer Configuration
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // Consumer Configuration
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaGroupID);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);

        // Настройка десериализатора ключа
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Настройка десериализатора значения с обработкой ошибок
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());

        // Указание доверенных пакетов
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.messagingapp.dto");

        // Маппинг типов для автоматического определения DTO
        config.put(JsonDeserializer.TYPE_MAPPINGS,
                "messageRequest:com.example.messagingapp.dto.MessageRequest," +
                        "messageResponse:com.example.messagingapp.dto.MessageResponse");

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        // Настройка обработки ошибок (3 попытки с интервалом 1 секунда)
        CommonErrorHandler errorHandler = new DefaultErrorHandler(
                (consumerRecord, exception) -> log.error("Ошибка обработки сообщения. Topic: {}, Key: {}, Value: {}",
                        consumerRecord.topic(),
                        consumerRecord.key(),
                        consumerRecord.value(),
                        exception),
                new FixedBackOff(interval, maxAttempts)
        );
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    // Topic Configuration
    @Bean
    public NewTopic inputTopic() {
        return TopicBuilder.name(inputTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic outputTopic() {
        return TopicBuilder.name(outputTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
