# Практическое задание для кандидатов

### Описание задания
Необходимо разработать микросервис на Java с использованием Spring Boot, который будет:

1. Считывать сообщения из Kafka топика input-messages
2. Обрабатывать полученные данные
3. Сохранять результаты в базу данных PostgreSQL
4. Отправлять подтверждение обработки в Kafka топик output-messages
5. Через Swagger можно посмотреть статистику отправленных сообщений

### Технологический стек

- **Java 17**
- **Spring Boot 3.1.5**
- **Spring Kafka** - для работы с Kafka
- **Spring Data JPA** - для ORM и доступа к данным
- **PostgreSQL** - в качестве хранилища данных
- **Lombok** - для уменьшения бойлерплейт-кода
- **Docker & Docker Compose** - для контейнеризации и оркестрации

## Формат сообщений

### Входящее сообщение (`input-messages` топик)
```json
{
  "id": "uuid-string",
  "content": "текст сообщения",
  "timestamp": "2023-01-01T12:00:00Z"
}
```

### Исходящее сообщение (`output-messages` топик) (важное замечание, `messageId` это `id` из сообщения выше)
```json
{
  "messageId": "uuid-string" ,
  "status": "PROCESSED",
  "processedAt": "2023-01-01T12:01:00Z"
}
```

## Настройка и запуск

### Предварительные требования

- Docker и Docker Compose
- JDK 17 (для локальной разработки)
- Maven/Gradle (для локальной разработки)

### Локальный запуск (для разработки)

1. Запустите инфраструктурные компоненты:
   ```bash
   docker compose up -d
   ```

## Тестирование

### Отправка тестового сообщения

Для отправки тестового сообщения в топик `input-messages` можно использовать скрипт Kafka CLI:

```bash
# Запуск консольного продюсера
docker exec -it kafka bash
kafka-topics --create --topic input-messages --bootstrap-server kafka:9092 --partitions 1 --replication-factor 1
kafka-console-producer --topic input-messages --bootstrap-server kafka:9092
# Затем введите JSON сообщение:
{"id":"123e4567-e89b-12d3-a456-426614174001","content":"Тестовое сообщение 1","timestamp":"2023-01-01T12:00:00Z"}
```

### Просмотр отправленных сообщений

На порту 8080 вашего компьютера будет UI для просмотра сообщений в Kafka

### Проверка базы данных

```bash
# Подключение к PostgreSQL
docker exec -it postgres psql -U user -d messagedb

# SQL запрос для просмотра сохраненных сообщений
SELECT * FROM messages;
```

## Требования к проекту

- Код должен быть размещен в GitHub репозитории 
- Должен присутствовать файл README.md с инструкциями по запуску
- В корне проекта должен быть файл docker-compose.yml, который поднимает:

- - PostgreSQL
- - Zookeeper
- - Kafka с предварительно созданными топиками input-messages и output-messages
- - Сам микросервис

- Проект должен запускаться командой `docker compose up`
- Покрытие кода unit-тестами будет преимуществом

## Особенности реализации

### Обработка ошибок

- Приложение обрабатывает исключения при получении и обработке сообщений
- В случае ошибки, информация логируется, но приложение продолжает работу с новыми сообщениями

### Транзакционность

- Операции сохранения в базу данных выполняются в транзакциях
- Гарантируется атомарность операций записи в БД

## Дополнительные ресурсы

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Kafka Documentation](https://docs.spring.io/spring-kafka/docs/current/reference/html/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
