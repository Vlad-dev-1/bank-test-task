package com.example.messagingapp.config;


import jakarta.annotation.PostConstruct;
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

    @Value("${spring.kafka.topics.inputTopic.name}")
    private String inputTopic;

    @Value("${spring.kafka.topics.outputTopic.name}")
    private String outputTopic;

    @Value("${spring.kafka.consumer.group-id}")
    private String kafkaGroupID;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    @Value("#{${spring.kafka.listener.retry.interval}}")
    private Long interval;

    @Value("#{${spring.kafka.listener.retry.max-attempts}}")
    private Long maxAttempts;

    @Value("${spring.kafka.consumer.isolation-level}")
    private String consumerIsolationLevel;

    @Value("${spring.kafka.producer.acks}")
    private String producerAcks;

    @Value("${spring.kafka.producer.retries}")
    private String producerRetries;

    @Value("${spring.kafka.producer.enable-idempotence}")
    private String producerIdempotence;


    @PostConstruct
    public void init() {
        log.info("Инициализация Kafka конфигурации для сервера: {}", bootstrapServers);
        log.info("Топик для входящих сообщений: {}, Топик для исходящих сообщений: {}", inputTopic, outputTopic);
        log.info("Группа потребителя: {}, Автосмещение: {}", kafkaGroupID, autoOffsetReset);
    }

    // Producer Configuration
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, producerAcks);  // Ждём подтверждения от всех реплик
        configProps.put(ProducerConfig.RETRIES_CONFIG, producerRetries);  // 3 попытки при ошибках
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, producerIdempotence);  // Идемпотентность

        log.info("Создание ProducerFactory с конфигурацией: {}", configProps);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate<String, Object> kafkaTemplate = new KafkaTemplate<>(producerFactory());

        log.info("KafkaTemplate создан и настроен");
        return kafkaTemplate;
    }

    // Consumer Configuration
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaGroupID);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        config.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, consumerIsolationLevel);  // Для транзакций

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

        log.info("Создание ConsumerFactory с конфигурацией: {}", config);
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        // Настройка обработки ошибок (3 попытки с интервалом 1 секунда)
        CommonErrorHandler errorHandler = new DefaultErrorHandler(
                (record, exception) ->
                        log.error("!!!Сообщение не получено после всех попыток. Топик: {}, Ключ: {}",
                                record.topic(), record.key(), exception),
                new FixedBackOff(interval, maxAttempts)
        );
        factory.setCommonErrorHandler(errorHandler);

        log.info("Настроен слушатель Kafka с {} попытками и интервалом {}мс", maxAttempts, interval);
        return factory;
    }

    // Topic Configuration
    @Bean
    public NewTopic inputTopic(@Value("#{${spring.kafka.topics.inputTopic.partitions}}") int partitions,
                               @Value("#{${spring.kafka.topics.inputTopic.replicas}}") int replicas,
                               @Value("${spring.kafka.topics.inputTopic.min-insync-replicas}") String minInsyncReplicas) {

        NewTopic topic = TopicBuilder.name(inputTopic)
                .partitions(partitions)
                .replicas(replicas)
                .config("min.insync.replicas", minInsyncReplicas)  // Важно для отказоустойчивости
                .build();

        log.info("Создание топика для входящих сообщений: {} с {} партициями и реплики {}",
                inputTopic,
                topic.numPartitions(),
                topic.replicationFactor());
        return topic;
    }

    @Bean
    public NewTopic outputTopic(@Value("#{${spring.kafka.topics.outputTopic.partitions}}") int partitions,
                                @Value("#{${spring.kafka.topics.outputTopic.replicas}}") int replicas,
                                @Value("${spring.kafka.topics.outputTopic.min-insync-replicas}") String minInsyncReplicas) {

        NewTopic topic = TopicBuilder.name(outputTopic)
                .partitions(partitions)
                .replicas(replicas)
                .config("min.insync.replicas", minInsyncReplicas)  // Важно для отказоустойчивости
                .build();
        log.info("Создание топика для исходящих сообщений: {} с {} партициями и реплики {}",
                inputTopic,
                topic.numPartitions(),
                topic.replicationFactor());
        return topic;
    }
}
