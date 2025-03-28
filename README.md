# Микросервис обработки сообщений - Практическое задание

## Описание задания

Необходимо разработать микросервис на Java с использованием Spring Boot для обработки сообщений. Проект разделен на два уровня сложности, выберите наиболее подходящий для вас:

### Уровень 1: Базовое REST API с PostgreSQL
- Создание REST API для приема и обработки сообщений
- Сохранение данных в PostgreSQL
- Предоставление API для получения статистики
- Базовое покрытие тестами
- Swagger документация для API

### Уровень 2: Полная реализация с Kafka
- Все функции из Уровня 1
- Интеграция с Kafka для асинхронной обработки сообщений
- Реализация consumer/producer для работы с топиками
- Дополнительная конфигурация для обеспечения отказоустойчивости

## Технологический стек

* **Java 17**
* **Spring Boot 3.1.5**
* **Spring Kafka** (для Уровня 2) - для работы с Kafka
* **Spring Data JPA** - для ORM и доступа к данным
* **PostgreSQL** - в качестве хранилища данных
* **Lombok** - для уменьшения бойлерплейт-кода
* **Swagger/OpenAPI** - для документации API
* **Docker & Docker Compose** - для контейнеризации и оркестрации

## Форматы данных

### Уровень 1: REST API

#### Входящее сообщение (POST /api/messages)
```json
{
  "id": "uuid-string",
  "content": "текст сообщения",
  "timestamp": "2023-01-01T12:00:00Z"
}
```

#### Ответ API (важное замечание, `messageId` это `id` из сообщения выше)
```json
{
  "messageId": "uuid-string",
  "status": "PROCESSED",
  "processedAt": "2023-01-01T12:01:00Z"
}
```

### Уровень 2: Kafka сообщения

#### Входящее сообщение (`input-messages` топик)
```json
{
  "id": "uuid-string",
  "content": "текст сообщения",
  "timestamp": "2023-01-01T12:00:00Z"
}
```

#### Исходящее сообщение (`output-messages` топик) (важное замечание, `messageId` это `id` из сообщения выше)
```json
{
  "messageId": "uuid-string",
  "status": "PROCESSED",
  "processedAt": "2023-01-01T12:01:00Z"
}
```

## Настройка и запуск

### Предварительные требования
* Docker и Docker Compose
* JDK 17 (для локальной разработки)
* Maven/Gradle (для локальной разработки)

### Локальный запуск (для разработки)

#### Уровень 1
Запустите только PostgreSQL:
```shell
docker compose up -d postgres
```

#### Уровень 2
Запустите все инфраструктурные компоненты:
```shell
docker compose up -d
```

## Эндпоинты API (Уровень 1)

- `POST /api/messages` - Отправить новое сообщение
- `GET /api/messages` - Получить список всех сообщений
- `GET /api/messages/{id}` - Получить сообщение по ID
- `GET /api/statistics` - Получить статистику обработанных сообщений

## Тестирование

### Уровень 1: REST API
Используйте curl, Postman или любой другой HTTP-клиент:

```bash
# Отправка сообщения
curl -X POST http://localhost:8080/api/messages \
  -H "Content-Type: application/json" \
  -d '{"id":"123e4567-e89b-12d3-a456-426614174001","content":"Тестовое сообщение 1","timestamp":"2023-01-01T12:00:00Z"}'

# Получение всех сообщений
curl -X GET http://localhost:8080/api/messages

# Получение статистики
curl -X GET http://localhost:8080/api/statistics
```

### Уровень 2: Kafka
Для отправки тестового сообщения в топик `input-messages`:

```bash
# Запуск консольного продюсера
docker exec -it kafka bash
kafka-topics --create --topic input-messages --bootstrap-server kafka:9092 --partitions 1 --replication-factor 1
kafka-console-producer --topic input-messages --bootstrap-server kafka:9092

# Затем введите JSON сообщение:
{"id":"123e4567-e89b-12d3-a456-426614174001","content":"Тестовое сообщение 1","timestamp":"2023-01-01T12:00:00Z"}
```

Для проверки сообщений в выходном топике:
```bash
# Запуск консольного консьюмера
docker exec -it kafka bash
kafka-console-consumer --topic output-messages --bootstrap-server kafka:9092 --from-beginning
```

Ну или просто зайти в UI Kafka, по дефолту он на 8080 порту

### Проверка базы данных

```bash
# Подключение к PostgreSQL
docker exec -it postgres psql -U user -d messagedb

# SQL запрос для просмотра сохраненных сообщений
SELECT * FROM messages;
```

## Требования к проекту

* Код должен быть размещен в GitHub репозитории
* Должен присутствовать файл README.md с инструкциями по запуску
* В корне проекта должен быть файл docker-compose.yml для запуска компонентов:
   * PostgreSQL (для всех уровней)
   * Zookeeper и Kafka (для Уровня 2)
   * Сам микросервис
* Проект должен запускаться командой `docker compose up`
* Покрытие кода unit-тестами будет преимуществом

## Особенности реализации

### Обработка ошибок
* Приложение должно корректно обрабатывать исключения
* В случае ошибки, информация логируется, но приложение продолжает работу с новыми запросами/сообщениями

### Транзакционность
* Операции сохранения в базу данных выполняются в транзакциях
* Гарантируется атомарность операций записи в БД

## Рекомендуемая структура проекта

```
src/
├── main/
│   ├── java/com/example/messagingapp/
│   │   ├── config/           # Конфигурационные классы
│   │   ├── controller/       # REST контроллеры (Уровень 1)
│   │   ├── dto/              # Объекты передачи данных
│   │   ├── entity/           # JPA сущности
│   │   ├── exception/        # Пользовательские исключения
│   │   ├── kafka/            # Компоненты для работы с Kafka (Уровень 2)
│   │   │   ├── consumer/     # Потребители Kafka
│   │   │   └── producer/     # Производители Kafka
│   │   ├── repository/       # Spring Data репозитории
│   │   ├── service/          # Бизнес-логика
│   │   └── MessageAppApplication.java  # Основной класс приложения
│   └── resources/
│       ├── application.yml   # Конфигурация приложения
│       └── db/migration/     # SQL миграции (если используется Flyway)
└── test/                     # Тесты
docker-compose.yml            # Docker компоненты
README.md                     # Документация
```

## Ключевые компоненты

### Уровень 1: REST API
1. **Модель данных (Entity)**:
   - Создайте сущность `Message` с необходимыми полями
   - Добавьте enum для статусов сообщений

2. **Репозиторий**:
   - Создайте JPA репозиторий для работы с сообщениями
   - Добавьте необходимые методы запросов

3. **Сервисный слой**:
   - Реализуйте бизнес-логику обработки сообщений
   - Добавьте транзакционное управление

4. **REST контроллеры**:
   - Создайте API для приема и обработки сообщений
   - Добавьте API для статистики

### Уровень 2: Kafka Integration
1. **Конфигурация Kafka**:
   - Настройте consumer и producer для работы с топиками
   - Добавьте конфигурацию десериализации и сериализации

2. **Kafka Consumer**:
   - Реализуйте прослушивание топика `input-messages`
   - Добавьте обработку полученных сообщений

3. **Kafka Producer**:
   - Реализуйте отправку сообщений в топик `output-messages`
   - Добавьте обработку ошибок при отправке

## Дополнительные ресурсы
* [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
* [Spring Kafka Documentation](https://docs.spring.io/spring-kafka/reference/html/)
* [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
* [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
* [Docker Compose Documentation](https://docs.docker.com/compose/)
