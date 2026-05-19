# Полное исследование структуры проекта English Words App

## Обзор проекта

Spring Boot 3.5 приложение для изучения английских слов с:
- Thymeleaf + CSS (custom, без Tailwind CDN) — серверный рендеринг
- PostgreSQL + Liquibase — БД и миграции
- Spring Security — OAuth2 (Google) + JWT + form login
- Слоистая архитектура: Controller → Service → Repository

---

## 1. МОДЕЛИ (JPA-сущности)

### 1.1 WordCard (`src/main/java/com/example/englishwordsapp/model/WordCard.java`, 78 строк)

```java
@Entity
@Table(name = "word_cards")
@Getter @Setter @ToString @NoArgsConstructor @AllArgsConstructor
public class WordCard {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "English word is required")
    @Column(nullable = false)
    private String englishWord;

    @NotBlank(message = "Translation is required")
    @Column(nullable = false)
    private String translation;

    @Column(columnDefinition = "TEXT")
    private String example;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel = DifficultyLevel.BEGINNER;

    @ManyToMany(mappedBy = "wordCards")
    @ToString.Exclude
    private Set<WordSet> wordSets = new HashSet<>();

    public enum DifficultyLevel { BEGINNER, INTERMEDIATE, ADVANCED }
}
```

**Таблица**: `word_cards` | **Связи**: M:N с `WordSet` через `word_set_words` (inverse side)

### 1.2 WordSet (`src/main/java/com/example/englishwordsapp/model/WordSet.java`, 96 строк)

```java
@Entity
@Table(name = "word_sets")
@Getter @Setter @ToString @NoArgsConstructor @AllArgsConstructor
public class WordSet {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @ToString.Exclude
    private User owner;

    @Column(name = "is_system", nullable = false)
    private boolean isSystem = false;

    @Column(name = "is_visible", nullable = false)
    private boolean isVisible = true;

    @ManyToMany
    @JoinTable(
        name = "word_set_words",
        joinColumns = @JoinColumn(name = "word_set_id"),
        inverseJoinColumns = @JoinColumn(name = "word_card_id")
    )
    @ToString.Exclude
    private Set<WordCard> wordCards = new HashSet<>();

    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
}
```

**Таблица**: `word_sets` | **Связи**: M:N с `WordCard` через `word_set_words` (owning side), M:1 с `User`

### 1.3 UserCard (`src/main/java/com/example/englishwordsapp/model/UserCard.java`, 76 строк)

```java
@Entity
@Table(name = "user_cards", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "card_id"}))
public class UserCard {
    @Id @GeneratedValue private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") private User user;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "card_id") private WordCard wordCard;
    private LocalDateTime addedAt;
    @Enumerated(EnumType.STRING) private StudyStatus status = StudyStatus.LEARNING;

    public enum StudyStatus { LEARNING, KNOWN }
}
```

**Таблица**: `user_cards` | Связывает User → WordCard с статусом изучения

### 1.4 UserSetSubscription (`src/main/java/com/example/englishwordsapp/model/UserSetSubscription.java`, 66 строк)

```java
@Entity
@Table(name = "user_set_subscriptions", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "word_set_id"}))
public class UserSetSubscription {
    @Id @GeneratedValue private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") private User user;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "word_set_id") private WordSet wordSet;
    private LocalDateTime subscribedAt;
}
```

### 1.5 WordSetFork (`src/main/java/com/example/englishwordsapp/model/WordSetFork.java`, 71 строка)

```java
@Entity
@Table(name = "word_set_forks", uniqueConstraints = @UniqueConstraint(columnNames = {"original_set_id", "forked_set_id"}))
public class WordSetFork {
    @Id @GeneratedValue private Long id;
    @ManyToOne @JoinColumn(name = "original_set_id") private WordSet originalSet;
    @ManyToOne @JoinColumn(name = "forked_set_id") private WordSet forkedSet;
    @ManyToOne @JoinColumn(name = "user_id") private User user;
    private LocalDateTime forkedAt;
}
```

### 1.6 StudySession (`model/StudySession.java`, 84 строки)

```java
@Entity @Table(name = "study_sessions")
public class StudySession {
    public enum ExerciseType { TEXT_INPUT, MULTIPLE_CHOICE }
    @Id @GeneratedValue private Long id;
    @ManyToOne @JoinColumn(name = "user_id") private User user;
    private LocalDateTime startedAt, endedAt;
    @Enumerated(EnumType.STRING) private ExerciseType exerciseType;
    private int wordCount, correctCount, currentIndex;
    @Column(columnDefinition = "TEXT") private String wordIdsOrder; // comma-separated IDs
}
```

### 1.7 StudyResult (`model/StudyResult.java`, 81 строка)

```java
@Entity @Table(name = "study_results")
public class StudyResult {
    public enum ExerciseType { TEXT_INPUT, MULTIPLE_CHOICE }
    @Id @GeneratedValue private Long id;
    @ManyToOne @JoinColumn(name = "study_session_id") private StudySession studySession;
    @ManyToOne @JoinColumn(name = "word_card_id") private WordCard wordCard;
    @ManyToOne @JoinColumn(name = "word_set_id") private WordSet wordSet;
    private boolean isCorrect;
    @Enumerated(EnumType.STRING) private ExerciseType exerciseType;
    private LocalDateTime answeredAt;
}
```

### 1.8 UserWordProgress (`model/UserWordProgress.java`, 78 строк)

```java
@Entity @Table(name = "user_word_progress",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "word_card_id", "word_set_id"}))
public class UserWordProgress {
    @Id @GeneratedValue private Long id;
    @ManyToOne @JoinColumn(name = "user_id") private User user;
    @ManyToOne @JoinColumn(name = "word_card_id") private WordCard wordCard;
    @ManyToOne @JoinColumn(name = "word_set_id") private WordSet wordSet;
    private int totalAttempts = 0;
    private int correctAnswers = 0;
    private LocalDateTime lastPracticedAt;
}
```

### 1.9 User (`security/User.java`, 67 строк)

```java
@Data @Entity @Table(name = "users")
public class User {
    @Id @GeneratedValue private Long id;
    @Column(unique = true, nullable = false) private String username;
    @Column(unique = true, nullable = false) private String email;
    private String password, fullName, avatar;
    private boolean enabled = true, accountNonLocked = true;
    @Enumerated(EnumType.STRING) private AuthProvider provider = AuthProvider.LOCAL;
    private String providerId;
    @ManyToMany(fetch = FetchType.EAGER) private Set<Role> roles = new HashSet<>();
    private LocalDateTime createdAt, updatedAt;
}
```

### 1.10 Role (`security/Role.java`, 25 строк)

```java
@Data @Entity @Table(name = "roles")
public class Role {
    @Id @GeneratedValue private Long id;
    @Column(unique = true, nullable = false) private String name;
    private String description;
}
```

---

## 2. КОНТРОЛЛЕРЫ

### 2.1 WordCardController (`controller/WordCardController.java`, 227 строк) — MVC

**Маппинг**: `/cards`

| Метод | URL | Описание |
|-------|-----|----------|
| `GET /cards` | `listCards` | Список карточек пользователя (с фильтрацией по setId) |
| `GET /cards/new` | `showAddCardForm` | Форма добавления слова (поиск + создание) |
| `POST /cards` | `addCardToCollection` | Добавить существующую/новую карточку в коллекцию |
| `GET /cards/{id}` | `viewCard` | Просмотр карточки (flashcard с флипом) |
| `POST /cards/{id}/add` | `addToCollection` | Добавить в коллекцию |
| `POST /cards/{id}/remove` | `removeFromCollection` | Удалить из коллекции |
| `POST /cards/{id}/status` | `updateStatus` | Обновить статус изучения |
| `GET /cards/random` | `randomCard` | Случайная карточка |
| `GET /cards/search-global` | `searchGlobalPool` | REST-поиск по глобальному пулу (для AJAX) |
| `GET /cards/{id}/edit` | `showEditCardForm` | Форма редактирования |
| `POST /cards/{id}` | `updateCard` | Обновить карточку |
| `POST /cards/{id}/delete` | `deleteCard` | Удалить карточку |

**Зависимости**: `WordCardService`, `WordSetService`

**Ключевая логика `addCardToCollection`**:
- Если `wordCard.id != null` — добавляет существующую карточку (из поиска)
- Если `setId` указан — добавляет в конкретный набор
- Иначе — создаёт новую карточку и добавляет в дефолтный набор "Мои словечки"

### 2.2 WordSetController (`controller/WordSetController.java`, 101 строка) — MVC

**Маппинг**: `/sets`

| Метод | URL | Описание |
|-------|-----|----------|
| `GET /sets` | `listSets` | Список всех наборов (мои, доступные, подписки) |
| `GET /sets/new` | `showCreateForm` | Форма создания набора |
| `POST /sets/create` | `createSet` | Создать набор |
| `GET /sets/{id}` | `viewSet` | Детали набора (слова, подписка, форк) |
| `POST /sets/{id}/subscribe` | `subscribe` | Подписаться |
| `POST /sets/{id}/unsubscribe` | `unsubscribe` | Отписаться |
| `POST /sets/{id}/fork` | `fork` | Форкнуть набор |

**Зависимости**: `WordSetService`

### 2.3 WordCardRestController (`controller/WordCardRestController.java`, 117 строк) — REST API

**Маппинг**: `/api/cards`

| Метод | URL | Описание |
|-------|-----|----------|
| `GET /api/cards` | Все карточки |
| `GET /api/cards/{id}` | Карточка по ID |
| `POST /api/cards` | Создать карточку |
| `PUT /api/cards/{id}` | Обновить |
| `DELETE /api/cards/{id}` | Удалить |
| `GET /api/cards/random` | Случайная |
| `GET /api/cards/search` | Поиск |
| `GET /api/cards/difficulty/{level}` | По сложности |
| `GET /api/cards/my` | Мои карточки |
| `GET /api/cards/my/ids` | ID моих карточек |
| `POST /api/cards/{id}/add-to-collection` | Добавить в коллекцию |
| `DELETE /api/cards/{id}/remove-from-collection` | Удалить из коллекции |
| `PUT /api/cards/{id}/study-status` | Статус изучения |
| `GET /api/cards/search-global` | Поиск по глобальному пулу |

### 2.4 WordSetRestController (`controller/WordSetRestController.java`, 89 строк) — REST API

**Маппинг**: `/api/sets`

| Метод | URL | Описание |
|-------|-----|----------|
| `GET /api/sets` | Доступные наборы |
| `GET /api/sets/my` | Мои наборы |
| `GET /api/sets/system` | Системные наборы (публичный) |
| `POST /api/sets` | Создать набор |
| `GET /api/sets/{id}/words` | Слова набора |
| `POST /api/sets/{id}/subscribe` | Подписаться |
| `DELETE /api/sets/{id}/subscribe` | Отписаться |
| `POST /api/sets/{id}/fork` | Форкнуть |

### 2.5 StudyController (`controller/StudyController.java`, 172 строки) — MVC

**Маппинг**: `/study`

| Метод | URL | Описание |
|-------|-----|----------|
| `GET /study` | Выбор наборов и типа упражнения |
| `POST /study/start` | Начать сессию |
| `GET /study/session/{id}` | Текущее слово сессии |
| `POST /study/session/{id}/answer` | Отправить ответ |
| `GET /study/session/{id}/next` | Следующее слово |
| `GET /study/session/{id}/results` | Результаты сессии |

### 2.6 AnalyticsController (`controller/AnalyticsController.java`, 112 строк) — MVC

**Маппинг**: `/analytics`

| Метод | URL | Описание |
|-------|-----|----------|
| `GET /analytics` | Обзор статистики по наборам |
| `GET /analytics/sets/{id}` | Детали набора с прогрессом по словам |
| `GET /analytics/sets/{id}/words/{wordId}` | Детали слова |

### 2.7 AuthController (`controller/AuthController.java`, 132 строки) — Mixed MVC + REST

| Метод | URL | Описание |
|-------|-----|----------|
| `POST /api/auth/login` | JWT-аутентификация (REST) |
| `GET /login` | Страница входа (Thymeleaf) |
| `GET /register` | Страница регистрации |
| `POST /register` | Регистрация пользователя |

---

## 3. СЕРВИСЫ

### 3.1 WordSetService (`service/WordSetService.java`, 199 строк)

**Ключевые методы**:
- `getSystemSets()` — системные наборы (`isSystem=true`)
- `getUserSets(userId)` — наборы пользователя (`owner_id=userId`)
- `getAvailableSets(userId)` — наборы пользователя + системные
- `getSubscribedSets(userId)` — наборы с подписками
- `getSetWords(setId)` — слова из набора
- `createSet(userId, name, desc)` — создать набор
- `createDefaultUserSet(userId)` — создать "Мои словечки"
- `getOrCreateDefaultUserSet(userId)` — найти или создать дефолтный набор по имени "Мои словечки"
- `deleteSet(setId, userId)` — удалить (нельзя системные)
- `addWordToSet(setId, wordCardId, userId)` — добавить слово в набор (с проверкой прав)
- `removeWordFromSet(setId, wordCardId, userId)` — удалить слово из набора
- `subscribeToSet(userId, setId)` — подписка
- `unsubscribeFromSet(userId, setId)` — отписка
- `forkSet(userId, originalSetId)` — форк набора (копия всех слов)
- `validateCanModify(wordSet, userId)` — проверка: не системный + владелец

### 3.2 WordCardService (`service/WordCardService.java`, 168 строк)

**Ключевые методы**:
- `createCardAndAddToUserCollection(userId, wordCard)` — создать карточку + добав��ть в "Мои словечки" + user_cards
- `addCardToUserCollection(userId, cardId)` — добавить существующую карточку в дефолтный набор + user_cards
- `removeCardFromUserCollection(userId, cardId)` — удалить из набора + user_cards
- `getUserCards(userId)` — слова из дефолтного набора "Мои словечки"
- `getUserCardsFromSet(setId)` — слова из конкретного набора
- `searchGlobalPool(query)` — поиск по `englishWord` (LIKE, case-insensitive)

### 3.3 StudyService (`service/StudyService.java`, 213 строк)

- Управление сессиями изучения
- Проверка ответов (TEXT_INPUT: сравнение с translation; MULTIPLE_CHOICE: сравнение ID)
- Генерация вариантов ответов для multiple choice

### 3.4 AnalyticsService (`service/AnalyticsService.java`, 186 строк)

- Статистика по наборам и словам
- Обновление прогресса (`updateProgress`)

---

## 4. РЕПОЗИТОРИИ

### WordSetRepository (`repository/WordSetRepository.java`)
```java
List<WordSet> findByOwnerId(Long ownerId);
List<WordSet> findByIsSystemTrue();
List<WordSet> findByNameContainingIgnoreCase(String name);
@Query("SELECT ws FROM WordSet ws WHERE ws.owner.id = :ownerId OR ws.isSystem = true")
List<WordSet> findByOwnerIdOrIsSystemTrue(@Param("ownerId") Long ownerId);
Optional<WordSet> findByNameAndOwnerId(String name, Long ownerId);
```

### WordCardRepository (`repository/WordCardRepository.java`)
```java
List<WordCard> findByDifficultyLevel(WordCard.DifficultyLevel difficultyLevel);
List<WordCard> findByEnglishWordContainingIgnoreCase(String word);
@Query(value = "SELECT * FROM word_cards ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
Optional<WordCard> findRandomCard();
```

### UserCardRepository (`repository/UserCardRepository.java`)
```java
List<UserCard> findByUserId(Long userId);
Optional<UserCard> findByUserIdAndWordCardId(Long userId, Long cardId);
boolean existsByUserIdAndWordCardId(Long userId, Long cardId);
void deleteByUserIdAndWordCardId(Long userId, Long cardId);
List<UserCard> findByUserIdAndStatus(Long userId, StudyStatus status);
@Query("SELECT uc.wordCard.id FROM UserCard uc WHERE uc.user.id = :userId")
List<Long> findCardIdsByUserId(@Param("userId") Long userId);
```

### UserSetSubscriptionRepository (`repository/UserSetSubscriptionRepository.java`)
```java
List<UserSetSubscription> findByUserId(Long userId);
Optional<UserSetSubscription> findByUserIdAndWordSetId(Long userId, Long wordSetId);
boolean existsByUserIdAndWordSetId(Long userId, Long wordSetId);
void deleteByUserIdAndWordSetId(Long userId, Long wordSetId);
```

### Остальные:
- **WordSetForkRepository**: `findByUserId`, `findByOriginalSetIdAndUserId`, `existsByOriginalSetIdAndUserId`
- **StudySessionRepository**: `findByUserId`, `findByUserIdAndStartedAtAfter`
- **StudyResultRepository**: `findByStudySessionId`, exercise type stats query
- **UserWordProgressRepository**: `findByUserIdAndWordSetId`, `findByUserIdAndWordCardIdAndWordSetId`
- **UserRepository**: `findByUsername`, `findByEmail`, `findByProviderAndProviderId`, `existsByUsername`, `existsByEmail`
- **RoleRepository**: `findByName`

---

## 5. ШАБЛОНЫ THYMELEAF

### Декоратор
- **layout.html** — базовый layout с CSRF meta, Google Fonts (Inter), `/css/main.css`, CSRF fetch interceptor, localStorage auth state

### Фрагменты
- **fragments/header.html** — навбар: My Cards, Sets, Add Word, Random, Study, Analytics, User info/Logout (auth) или Login/Register
- **fragments/card-form.html** (293 строки) — Форма добавления слова: поиск по глобальному пулу (AJAX `/cards/search-global`), выбор + автоматический submit, или создание нового слова (form). Содержит inline CSS и JS
- **fragments/card-detail.html** (130 строк) — Flashcard с 3D-флипом, статус изучения (LEARNING/KNOWN), кнопки Add/Remove
- **fragments/cards-list.html** (62 строки) — Поисковая страница с results grid
- **fragments/sets-list.html** (109 строк) — Три секции: My Sets, Available Sets, Subscribed Sets. Для каждого: View, Subscribe/Unsubscribe, Fork, Delete
- **fragments/set-detail.html** (74 строки) — Детали набора: описание, владелец, подписка/отписка/форк, список слов
- **fragments/set-form.html** (46 строк) — Простая фор��а создания набора (name + description)

### Основные страницы
- **index.html** (82 строки) — Список карточек пользователя (My Words). Empty state если нет карточек. Grid с difficulty badge + status badge + View/Remove
- **login.html** (82 строки) — Форма входа + Google OAuth
- **register.html** — Форма регистрации

### Study
- **study/choose.html** (93 строки) — Выбор наборов (checkbox) + тип упражнения (radio: TEXT_INPUT/MULTIPLE_CHOICE)
- **study/session.html** (103 строки) — Прогресс-бар, flashcard, TEXT_INPUT (input) или MULTIPLE_CHOICE (buttons)
- **study/results.html** (106 строк) — Результаты: summary (total/correct/incorrect/%), encouragement, таблица результатов

### Analytics
- **analytics/overview.html** — Обзор статистики по наборам
- **analytics/set-detail.html** — Детали набора с прогрессом по словам
- **analytics/word-detail.html** — Детали прогресса по слов��

---

## 6. SECURITY

### SecurityConfig (`config/SecurityConfig.java`, 107 строк)

- **CSRF**: отключен для `/api/**`
- **Session**: `IF_REQUIRED`
- **Публичные URL**: `/`, `/login`, `/register`, `/oauth2/**`, CSS/JS/images, `/api/auth/**`, `/api/sets/system`, GET `/api/cards`, search, random, search-global
- **Authenticated**: `/api/cards/my/**`, add-to-collection, remove, study-status, POST/PUT/DELETE cards, `/cards/**`, `/study/**`, `/analytics/**`
- **OAuth2**: Google, redirect после login → `/cards`
- **Form login**: `/login` page, redirect → `/cards`
- **JWT filter**: before `UsernamePasswordAuthenticationFilter`

### Другие security файлы:
- **AuthProvider.java** — enum: `LOCAL, GOOGLE, GITHUB, YANDEX, VK`
- **CustomUserDetails.java** — implements `UserDetails` + `OAuth2User`, поля: id, username, email, password, enabled, accountNonLocked, authorities, provider, providerId, attributes
- **CustomUserDetailsService.java** — загрузка по username или email
- **CustomOAuth2UserService.java** — обработка Google OAuth (создание/обновление пользователя)
- **JwtService.java** — генерация/валидация JWT токенов
- **JwtAuthenticationFilter.java** — извлечение JWT из `Authorization: Bearer ...`

---

## 7. LIQUIBASE МИГРАЦИИ

### `db.changelog-master.yaml` — порядок включения:
1. `001-create-user-cards-table.yaml` — таблица `user_cards` (user_id + card_id + status)
2. `002-load-english-vocabulary.yaml` — загрузка CSV словаря в `word_cards`
3. `003-create-word-sets.yaml` — таблицы `word_sets` + `word_set_words` (M:N)
4. `004-distribute-words-to-sets.yaml` — 5 системных наборов: Essential Words, Verbs & Actions, People & Family, Adjectives & Adverbs, Daily Life + SQL-распределение слов
5. `005-create-set-subscriptions-and-forks.yaml` — `user_set_subscriptions` + `word_set_forks`
6. `006-add-default-role.yaml` — вставка `ROLE_USER`
7. `007-create-study-tables.yaml` — `study_sessions` + `study_results`
8. `008-create-user-word-progress.yaml` — `user_word_progress`
9. `009-migrate-existing-user-cards.yaml` — миграция: для каждого пользователя из user_cards → создать "Мои словечки" + перенести слова в word_set_words

### Схема БД (таблицы):
| Таблица | Назначение |
|---------|-----------|
| `users` | Пользователи |
| `roles` | Роли (ROLE_USER) |
| `user_roles` | M:N users ↔ roles |
| `word_cards` | Словарные карточки (english_word, translation, example, notes, difficulty_level) |
| `word_sets` | Наборы слов (name, description, owner_id, is_system, is_visible) |
| `word_set_words` | M:N word_sets ↔ word_cards (PK: word_set_id + word_card_id) |
| `user_cards` | Карточки пользователя (user_id, card_id, status: LEARNING/KNOWN) |
| `user_set_subscriptions` | Подписки на наборы (user_id + word_set_id, unique) |
| `word_set_forks` | Форки наборов (original_set_id, forked_set_id, user_id) |
| `study_sessions` | Сессии изучения |
| `study_results` | Результаты ответов |
| `user_word_progress` | Прогресс по словам (user_id + word_card_id + word_set_id, unique) |

---

## 8. CSS (`src/main/resources/static/css/main.css`, 1530 строк)

Кастомный CSS без Tailwind, design system на CSS Custom Properties:

### Цвета:
- `--color-bg: #f8fafc`, `--color-surface: #ffffff`
- `--color-accent: #4f46e5` (индиго), `--color-success: #10b981`, `--color-danger: #ef4444`

### Шрифт: Inter (Google Fonts)

### Компоненты (определены в CSS):
- `.container`, `.page-content`, `.page-header`
- `.btn`, `.btn-primary`, `.btn-secondary`, `.btn-danger`, `.btn-ghost`, `.btn-sm`, `.btn-full`
- `.card`, `.card-item`, `.cards-grid` — сетка карточек
- `.card-surface` — surface container
- `.flashcard-container`, `.flashcard`, `.flashcard-inner`, `.flashcard-front`, `.flashcard-back` — 3D флип-карточки
- `.form-group`, `.form-label`, `.form-input`, `.form-actions`
- `.alert`, `.alert-success`, `.alert-error`
- `.navbar`, `.navbar-brand`, `.navbar-links`
- `.auth-page`, `.auth-container`, `.auth-form`, `.oauth-btn`
- `.difficulty-badge`, `.status-badge`
- `.sets-grid`, `.set-card`, `.set-card-header`, `.set-card-actions`
- `.results-summary`, `.result-stat`, `.results-table`
- `.progress-bar-container`, `.progress-bar`, `.progress-fill`
- `.checkbox-group`, `.radio-group`
- `.hero`, `.empty-state`

---

## 9. EXCEPTIONS

- **WordSetNotFoundException** — `RuntimeException("WordSet not found with id: " + id)`
- **WordSetAccessDeniedException** — `RuntimeException(message)` — нет доступа
- **SystemSetModificationException** — `RuntimeException("Cannot modify a system set")`

## 10. GLOBAL HANDLERS

- **RestExceptionHandler** (`@RestControllerAdvice`) — обработка validation errors (400), not found (404), access denied / system set modification (403)
- **GlobalSecurityAdvice** (`@ControllerAdvice`) — model attributes: `currentUser`, `isAuthenticated`, `username`

## 11. DTOs

- **LoginRequest** — `username`, `password` (оба `@NotBlank`)
- **RegisterRequest** — `username` (3-50 chars), `email` (@Email), `password` (min 6), `confirmPassword`

## 12. КОНФИГУРАЦИЯ (`application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/english_words
    username: postgres
    password: postgres
  jpa:
    hibernate.ddl-auto: update
    show-sql: true
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml
    enabled: true
  security.oauth2.client.registration.google:
    client-id: ${GOOGLE_CLIENT_ID}
    client-secret: ${GOOGLE_CLIENT_SECRET}
  thymeleaf.cache: false
server.port: 8080
app.jwt.secret: ${JWT_SECRET}
app.jwt.expiration-ms: 86400000
```

---

## 13. КЛЮЧЕВЫЕ АРХИТЕКТУРНЫЕ НАБЛЮДЕНИЯ

### Текущий механизм добавления слов:
1. **Через UI** (`/cards/new`): Поиск по глобальному пулу → клик → автоматический submit формы `addExistingForm` → `POST /cards` с `wordCard.id` → `addCardToUserCollection()` → добавляет в "Мои словечки" + `user_cards`
2. **Создание нового**: Заполнение формы → `POST /cards` без `wordCard.id` → `createCardAndAddToUserCollection()` → создаёт `WordCard`, добавляет в "Мои словечки" + `user_cards`
3. **REST API**: `POST /api/cards/{id}/add-to-collection` → `addCardToUserCollection()`

### Дефолтный набор "Мои словечки":
- Создаётся автоматически при первом добавлении слова через `getOrCreateDefaultUserSet(userId)`
- Поиск по имени: `wordSetRepository.findByNameAndOwnerId("Мои словечки", userId)`
- При добавлении слова → слово добавляется и в `word_set_words` (набор), и в `user_cards` (пользователь)

### Dual-storage:
- **word_set_words** — связь WordSet ↔ WordCard (M:N)
- **user_cards** — связь User ↔ WordCard с статусом (LEARNING/KNOWN)
- При добавлении/удалении обновляются обе таблицы

### Форма `card-form.html` поддерживает `setId` параметр:
- В `addCardToCollection` контроллера: если `setId != null` — добавляет в конкретный набор
- Но в текущей форме `card-form.html` нет UI для выбора набора (параметр `setId` не передаётся)
- В `showAddCardForm`: `model.addAttribute("userSets", userSets)` — наборы передаются, но в шаблоне не используются для выбора

### Отсутствующий функционал для добавления слов в наборы:
1. Нет UI для добавления слова в конкретный набор (не "Мои словечки")
2. В `set-detail.html` нет кнопки "Add word to this set"
3. В `card-form.html` нет dropdown для выбора набора
4. REST endpoint `POST /api/sets/{id}/words` для добавления слова в набор **отсутствует** (есть только `GET /api/sets/{id}/words`)
5. В `WordSetController` нет MVC endpoint для добавления слова в набор
