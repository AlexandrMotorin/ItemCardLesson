# AGENTS.md — English Words App

## Структура проекта

```
src/main/java/com/example/englishwordsapp/
├── config/          # Spring-конфигурации (SecurityConfig)
├── controller/      # MVC-контроллеры и REST-контроллеры
├── dto/             # Data Transfer Objects (LoginRequest, RegisterRequest)
├── exception/       # Кастомные исключения
├── global/          # Глобальные обработчики (@ControllerAdvice)
├── model/           # JPA-сущности (WordSet, WordCard, StudySession и др.)
├── repository/      # Spring Data JPA репозитории
├── security/        # JWT, OAuth2, User/Role, UserDetails
├── service/         # Бизнес-логика (WordSetService, StudyService и др.)
└── util/            # Утилиты (StringUtil)

src/main/resources/
├── application.yml            # Основная конфигурация
├── application-prod.yml       # Продакшн-профиль
├── db/changelog/              # Liquibase-миграции
├── static/css/                # CSS-стили
└── templates/                 # Thymeleaf-шаблоны (layout, login, study и др.)

src/test/java/com/example/englishwordsapp/
├── AbstractIntegrationTest.java   # Базовый класс интеграционных тестов
└── WordSetRepositoryTest.java     # Тест репозитория
src/test/resources/
└── application-test.yml           # Тестовая конфигурация
```

## Стек технологий

- **Java 17**, **Spring Boot 3.5**, **Gradle**
- **PostgreSQL 15** + **Liquibase** (миграции БД)
- **Spring Security** — OAuth2 (Google) + JWT-аутентификация
- **Thymeleaf** + Tailwind CSS — серверный рендеринг UI
- **Lombok** — генерация boilerplate-кода
- **Testcontainers** + JUnit 5 — интеграционные тесты

## Команды сборки и тестирования

| Команда | Описание |
|---------|----------|
| `./gradlew build` | Сборка проекта с тестами |
| `./gradlew test` | Запуск тестов (Testcontainers — нужен Docker) |
| `./gradlew bootRun` | Локальный запуск на порту 8080 |
| `docker compose up` | Запуск приложения + PostgreSQL в Docker |

**Переменные окружения** (обязательно для запуска):
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` — OAuth2
- `JWT_SECRET` — секрет для подписи JWT-токенов

## Стиль кода и правила именования

- **Отступы**: табуляция в Gradle-файлах, пробелы в Java-коде
- **Именование**: стандартный Java — `camelCase` для методов/полей, `PascalCase` для классов
- **Контроллеры**: MVC-контроллеры (`*Controller`) для Thymeleaf-страниц, REST-контроллеры (`*RestController`) для API
- **Сущности**: пакет `model/`, репозитории — `repository/`, сервисы — `service/`
- **Тесты**: интеграционные тесты наследуют `AbstractIntegrationTest`, используют Testcontainers для PostgreSQL
- **Миграции**: Liquibase-changelog в `src/main/resources/db/changelog/`

## Рекомендации по коммитам

Формат: **Conventional Commits** с указанием скоупа:
```
feat(auth): add user registration and improve authentication flows
feat: add personal word collection management
fix: <краткое описание>
```

- Префиксы: `feat`, `fix`, `refactor`, `docs`, `test`
- Скоуп опционален, но рекомендуется для крупных изменений
- PR должны содержать описание изменений и ссылку на issue (если есть)

## Архитектура и особенности

- **Слоистая архитектура**: Controller → Service → Repository
- **Доменные сущности**: `WordSet` → `WordCard`, `User` → `UserSetSubscription`, `StudySession` → `StudyResult`
- **Безопасность**: двойная аутентификация — OAuth2 (Google) для веб-интерфейса и JWT для API
- **Обработка ошибок**: `RestExceptionHandler` для REST, `GlobalSecurityAdvice` для MVC
