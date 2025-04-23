FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Создаем папку для логов
RUN mkdir -p /app/logs

# Копируем все необходимое
COPY .. .

# Собираем приложение
RUN ./gradlew :app:build --no-daemon

# Объявляем volume для логов
VOLUME /app/logs

# Запускаем
ENTRYPOINT ["java", "-jar", "app/build/libs/app.jar"]