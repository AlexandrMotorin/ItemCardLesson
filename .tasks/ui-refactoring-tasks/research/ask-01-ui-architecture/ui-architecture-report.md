# UI-слой english-words-app: Полный архитектурный анализ

## 1. Структура проекта

### Общие сведения
- **Тип**: Spring Boot 3.5.0 монолитное веб-приложение (Java 17)
- **Build system**: Gradle (single-module)
- **Модули**: один (`english-words-app`)
- **Путь**: `/home/sasha/OpenIDEProjects/ItemCardLesson`

### Организация кода
```
src/main/java/com/example/englishwordsapp/
├── config/           — SecurityConfig.java
├── controller/       — 7 контроллеров (Thymeleaf + REST)
├── dto/              — LoginRequest, RegisterRequest
├── exception/        — 3 кастомных исключения
├── global/           — GlobalSecurityAdvice, RestExceptionHandler
├── model/            — 8 JPA-сущностей
├── repository/       — 10 репозиториев
├── security/         — 7 классов (User, Role, JWT, OAuth2)
├── service/          — 4 сервиса
└── util/             — StringUtil

src/main/resources/
├── templates/        — 14 Thymeleaf-шаблонов
│   ├── layout.html
│   ├── index.html, login.html, register.html
│   ├── fragments/    — header, card-detail, card-form, cards-list, set-detail, set-form, sets-list
│   ├── study/        — choose, session, results
│   └── analytics/    — overview, set-detail, word-detail
├── static/css/       — main.css (1579 строк)
└── db/               — Liquibase миграции (9 changeset)
```

### Зависимости (build.gradle)
- `spring-boot-starter-web` — серверный рендеринг + REST API
- `spring-boot-starter-thymeleaf` — шаблонизатор
- `thymeleaf-layout-dialect` — layout decorator pattern
- `thymeleaf-extras-springsecurity6` — sec: namespace в шаблонах
- `spring-boot-starter-data-jpa` + PostgreSQL
- `spring-boot-starter-oauth2-client` — Google OAuth2
- `jjwt-api/impl/jackson` — JWT для REST API
- `spring-boot-starter-validation` — Bean Validation
- Lombok (compileOnly)

---

## 2. UI-фреймворк

### Основной подход: Server-Side Rendering (SSR) через Thymeleaf
- **НЕТ** Compose, React, Angular или других SPA-фреймворков
- **НЕТ** XML layouts (не Android-проект)
- Чистый **Thymeleaf** с layout dialect для декоратора
- Единственный CSS-файл `main.css` (1579 строк) — дизайн-система с CSS custom properties
- Минимальный inline JavaScript (fetch для search, flashcard flip)
- **Никаких** ��борщиков фронтенда (webpack, vite), CSS-препроцессоров, npm

### Layout-система
- `layout.html` — базовый layout с `<head>`, CSRF мета-теги, глобальные скрипты
- Все страницы используют `layout:decorate="layout"` (Thymeleaf Layout Dialect)
- `fragments/header.html` — навигационная панель (подключается через `th:replace`)

### Клиентский JavaScript
Встроен inline в шаблонах:
- **card-form.html**: ~150 строк JS — поиск слов, multi-set badge selection, авто-submit форм
- **card-detail.html**: flashcard flip анимация
- **layout.html**: CSRF token injection для fetch, localStorage state sync

---

## 3. Архитектурный паттерн

### Паттерн: **Classic MVC (Spring MVC)**
- **Нет MVVM**, нет ViewModel, нет State management, ��ет UseCase-классов
- Контроллеры **напрямую** вызывают сервисы и пробрасывают данные в Model
- Нет DTO-слоя между сервисами и шаблонами (кроме LoginRequest/RegisterRequest)
- Модели JPA передаются **прямо** в Thymeleaf-шаблоны

### Слои:
```
[Thymeleaf Templates] ←→ [Controllers] → [Services] → [Repositories] → [Database]
                                ↓
                          [Model (JPA entities)]
```

### Контроллеры (7 штук):

| Контроллер | Файл | Строки | Маршруты | Тип |
|---|---|---|---|---|
| AuthController | controller/AuthController.java | 132 | `/login`, `/register`, `/api/auth/login` | **Гибрид** (Thymeleaf + REST) |
| WordCardController | controller/WordCardController.java | 248 | `/cards/**` | Thymeleaf |
| WordCardRestController | controller/WordCardRestController.java | 117 | `/api/cards/**` | REST |
| WordSetController | controller/WordSetController.java | 105 | `/sets/**` | Thymeleaf |
| WordSetRestController | controller/WordSetRestController.java | 89 | `/api/sets/**` | REST |
| StudyController | controller/StudyController.java | 172 | `/study/**` | Thymeleaf |
| AnalyticsController | controller/AnalyticsController.java | 112 | `/analytics/**` | Thymeleaf |

### Сервисы (4 штуки):
| Сервис | Строки | Ответственность |
|---|---|---|
| WordCardService | 168 | CRUD слов, коллекции пользователя |
| WordSetService | 199 | CRUD наборов, подписки, форки |
| StudyService | 213 | Сессии изучения, проверка ответов |
| AnalyticsService | ~200+ | Статистика по сетам и словам |

---

## 4. Экраны и навигация

### Навигация — ссылочная (нет SPA-роутера)
Все переходы — полная перезагрузка страницы через HTTP redirect и ссылки.

### Карта экранов (14 шаблонов):

| Экран | Шаблон | URL | Описание |
|---|---|---|---|
| **Login** | `login.html` | `/login` | Вход (форма + Google OAuth2) |
| **Register** | `register.html` | `/register` | Регистрация |
| **My Cards** (главная) | `index.html` | `/cards` | Список слов пользователя |
| **Card Detail** | `fragments/card-detail.html` | `/cards/{id}` | Flashcard с 3D flip |
| **Add/Edit Card** | `fragments/card-form.html` | `/cards/new`, `/cards/{id}/edit` | Поиск + создание слова (376 строк!) |
| **Search Results** | `fragments/cards-list.html` | — | Результаты поиска |
| **Sets List** | `fragments/sets-list.html` | `/sets` | Список наборов (мои, доступные, подписанные) |
| **Set Detail** | `fragments/set-detail.html` | `/sets/{id}` | Набор с карточками |
| **Create Set** | `fragments/set-form.html` | `/sets/new` | Форма создания набора |
| **Study: Choose** | `study/choose.html` | `/study` | Выбор наборов и типа упражнения |
| **Study: Session** | `study/session.html` | `/study/session/{id}` | Интерактивная сессия (TEXT_INPUT / MULTIPLE_CHOICE) |
| **Study: Results** | `study/results.html` | `/study/session/{id}/results` | Результаты сессии |
| **Analytics: Overview** | `analytics/overview.html` | `/analytics` | Общая статистика |
| **Analytics: Set** | `analytics/set-detail.html` | `/analytics/sets/{id}` | Детальная статистика набора |
| **Analytics: Word** | `analytics/word-detail.html` | `/analytics/sets/{id}/words/{wordId}` | Статистика слова |

### Навигационная панель (`fragments/header.html`):
- My Cards → `/cards`
- Sets → `/sets`
- Add Word → `/cards/new`
- Random → `/cards/random`
- Study → `/study`
- Analytics → `/analytics`
- User info + Logout

---

## 5. Компоненты UI

### Переиспользуемые компоненты:
1. **Header** (`fragments/header.html`) — навбар, используется **во всех** шаблонах через `th:replace`
2. **Layout** (`layout.html`) — базовый layout через Thymeleaf Layout Dialect
3. **CSS Design System** (`main.css`) — custom properties, кнопки, карточки, формы, алерты, badges

### Ключевые UI-паттерны:
- **Cards Grid** (`.cards-grid`, `.card-item`) — сетка карточек слов (используется в index, set-detail, cards-list)
- **Sets Grid** (`.sets-grid`, `.set-card`) — сетка наборов
- **Flashcard** (`.flashcard`, `.flashcard-inner`) — 3D flip карточка
- **Results Summary** (`.results-summary`, `.result-stat`) — блоки статистики
- **Progress Bar** (`.progress-bar-container`, `.progress-bar`) — индикаторы прогресса
- **Alert** (`.alert`, `.alert-success`, `.alert-error`) — уведомления
- **Auth pages** (`.auth-page`, `.auth-container`, `.auth-form`) — страницы ав��оризации
- **Form controls** (`.form-group`, `.form-label`, `.form-input`) — элементы форм

### НЕ переиспользуемые, но повторяющиеся элементы:
- CSRF hidden input (`<input type="hidden" th:name="${_csrf.parameterName}"...>`) — дублируется в ~20 местах
- Проверка `if (userDetails == null) return "redirect:/login"` — дублируется в **каждом** методе контроллера
- Паттерн карточки слова (englishWord + translation + example + difficultyBadge) — дублируется в 4+ шаблонах

---

## 6. Проблемы и код-смеллы

### 🔴 Критические проблемы

#### P1: God-контроллер WordCardController (248 строк)
- **Файл**: `controller/WordCardController.java`
- 14 методов, смешивает: CRUD, коллекции, поиск, случайная карточка, edit/delete
- Слишком много ответственностей

#### P2: God-шаблон card-form.html (376 строк)
- **Файл**: `templates/fragments/card-form.html`
- Смешивает HTML-разметку, ~100 строк inline CSS (стили), ~150 строк inline JavaScript
- Содержит полноценную search autocomplete логику, multi-set selection, form validation — всё в одном файле

#### P3: Монолитный CSS (1579 строк)
- **Файл**: `static/css/main.css`
- Единственный CSS файл для всего приложения
- Нет разбиения на компоненты/модули
- Сложно поддерживать и находить стили

#### P4: Дублирование паттерна аутентификации
- `if (userDetails == null) return "redirect:/login"` повторяется в **17+ методах** контроллеров
- Вместо этого должна быть Spring Security конфигурация (уже частично есть в SecurityConfig)

### 🟠 Серьёзные проблемы

#### P5: JPA-сущности прямо в шаблонах
- WordCard, WordSet, UserCard, StudyResult — передаются напрямую в Thymeleaf
- Нет DTO-слоя для presentation
- Потенциальные N+1 проблемы, лишние данные в рендеринге, tight coupling

#### P6: Дублирование REST и Thymeleaf контроллеров
- `WordCardController` (Thymeleaf) и `WordCardRestController` (REST) дублируют логику
- `WordSetController` и `WordSetRestController` — то же самое
- `AuthController` смешивает REST (`/api/auth/login`) и Thymeleaf (`/login`, `/register`) в одном классе

#### P7: AnalyticsController содержит бизнес-логику
- **Файл**: `controller/AnalyticsController.java:55-78`
- Контроллер самостоятельно строит `WordWithProgress` список с расчётом статистики
- Вложенный DTO-класс `WordWithProgress` определён прямо внутри контроллера
- Эта логика должна быть в AnalyticsService

#### P8: AnalyticsController инжектит репозитории напрямую
- **Файл**: `controller/AnalyticsController.java:28-30`
- Контроллер инжектит `WordSetRepository`, `WordCardRepository`, `UserWordProgressRepository` минуя сервисный слой
- Нарушение слоёной архитектуры

#### P9: Inline стили в шаблонах
- `style="display:inline;"` — повторяется в 10+ местах для inline форм
- `style="text-align: center; margin-top: 32px;"` — в нескольких шаблонах
- В `card-detail.html:76-86` — inline стили для status badges вместо CSS-классов

### 🟡 Незначительные проблемы

#### P10: Дублирование UI-карточек слов
- Паттерн отображения карточки (englishWord + translation + example + difficultyBadge) дублируется в:
  - `index.html` (строки 44-71)
  - `fragments/set-detail.html` (строки 58-68)
  - `fragments/cards-list.html` (строки 41-58)
- Можно извлечь в Thymeleaf fragment

#### P11: Дублирование CSRF-инпутов
- `<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">` — ~20 мест
- Можно вынести в layout/fragment (или использовать th:action, который добавляет CSRF автоматически)

#### P12: Отсутствие error page
- Нет кастомной error page (404, 500)
- `RuntimeException` в контроллерах без graceful handling для Thymeleaf-страниц

#### P13: WordSetController.viewSet — неэффективный поиск
- **Файл**: `controller/WordSetController.java:61-63`
- Загружает ВСЕ available sets и фильтрует в памяти (`stream().filter()`) вместо прямого запроса по ID

#### P14: Непоследовательное именование шаблонов
- Шаблоны в `fragments/` — это полноценные страницы (card-detail, sets-list), а не фрагменты
- Настоящий фрагмент — только `header.html`
- Путает семантику: fragment vs page

---

## 7. Тесты UI

### Текущее состояние: **Тестов UI практически нет**

Найдено всего 2 тестовых файла:
1. `AbstractIntegrationTest.java` — базовый класс (SpringBootTest + PostgreSQL)
2. `WordSetRepositoryTest.java` — 4 теста репозитория (не UI!)

### Что отсутствует:
- ❌ **Нет UI-тестов** (ни Selenium, ни Playwright, ни Cypress)
- ❌ **Нет интеграционных тестов контроллеров** (MockMvc)
- ❌ **Нет unit-тестов сервисов**
- ❌ **Нет тестов шаблонов Thymeleaf**
- ❌ **Нет тестов REST API** (кроме WordSetRepositoryTest)
- ❌ **Нет E2E тестов**

---

## 8. Рекомендации по рефакторингу (по приоритетам)

### Приоритет 1 (Высокий) — Архитектурные проблемы

1. **Извлечь бизнес-логику из AnalyticsController в AnalyticsService** (P7, P8)
   - Перенести создание `WordWithProgress`, удалить прямой доступ к репозиториям
   
2. **Разделить WordCardController** (P1)
   - `WordCardViewController` — просмотр карточек
   - `WordCardFormController` — создание/редактирование
   - `WordCardCollectionController` — управление коллекцией

3. **Разделить AuthController** на REST и Thymeleaf части (P6)

4. **Устранить дублирование auth-проверки** (P4)
   - Уже есть SecurityConfig — нужно полагаться на него, а не проверять вручную

### Приоритет 2 (Средний) — UI-слой

5. **Разбить main.css на модули** (P3)
   - `base.css`, `components.css`, `pages/auth.css`, `pages/study.css`, `pages/analytics.css`
   
6. **Извлечь JavaScript из card-form.html** (P2)
   - Создать `static/js/card-form.js`
   - Создать `static/js/flashcard.js`
   
7. **Создать переиспользуемые Thymeleaf-фрагменты** (P10)
   - `fragments/word-card-item.html` — карточка слова
   - `fragments/alert.html` — уведомления
   
8. **Реорганизовать папку templates** (P14)
   - `pages/` для полноценных страниц, `fragments/` только для переиспользуемых частей

### Приоритет 3 (Низкий) — Качество

9. **Добавить DTO-слой** (P5) для передачи данных в шаблоны
10. **Убрать inline стили** (P9) в CSS-классы
11. **Добавить кастомные error pages** (P12)
12. **Оптимизировать WordSetController.viewSet** (P13) — прямой запрос по ID
13. **Добавить тесты** (Section 7) — хотя бы MockMvc для контроллеров

---

## 9. Сводная статистика

| Метрика | Значение |
|---|---|
| Контроллеры | 7 (4 Thymeleaf + 2 REST + 1 гибрид) |
| Сервисы | 4 |
| JPA-сущности | 8 |
| Репозитории | 10 |
| Thymeleaf-шаблоны | 14 |
| CSS-файлы | 1 (1579 строк) |
| JS-файлы | 0 (inline only) |
| Тесты | 2 файла (4 теста) |
| DTO-классы | 2 (LoginRequest, RegisterRequest) |
| Кастомные исключения | 3 |
| Строки кода (контроллеры) | ~1000 |
| Строки кода (шаблоны) | ~1700 |
