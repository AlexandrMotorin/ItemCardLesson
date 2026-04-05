# English Words App

Spring Boot приложение для изучения английских слов с карточками.

## Описание

Приложение предоставляет функционал для работы с карточками слов (WordCard), включая REST API и веб-интерфейс.

## Технологии

- Java
- Spring Boot
- Spring Security
- Gradle
- Docker

## Структура проекта

```
src/
├── main/
│   ├── java/com/example/englishwordsapp/
│   │   ├── config/          # Конфигурация безопасности
│   │   ├── controller/      # REST и веб контроллеры
│   │   ├── model/           # Модели данных
│   │   ├── repository/      # Репозитории
│   │   └── service/         # Бизнес-логика
│   └── resources/
└── test/
    └── java/                # Тесты
```

## Запуск приложения

### Через Gradle

```bash
./gradlew bootRun
```

### Через Docker

```bash
docker-compose up
```

## API Endpoints

- `GET /api/cards` - получить все карточки
- `POST /api/cards` - создать новую карточку
- `PUT /api/cards/{id}` - обновить карточку
- `DELETE /api/cards/{id}` - удалить карточку

## Тестирование

```bash
./gradlew test
```

## Лицензия

Проект создан в учебных целях.
