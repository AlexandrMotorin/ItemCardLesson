# UI Refactoring + Study MIXED Mode — Task Execution Plan

## Your Mission

Рефакторинг English Words App: убрать отдельный экран `/cards`, создать landing page, переключить навигацию на `/sets`, полностью переработать логику изучения слов (MIXED-режим с per-question типом упражнения и направлением перевода).

**Plan File:** `.tasks/ui-refactoring-tasks/PLAN.md`
**Tasks Directory:** `.tasks/ui-refactoring-tasks/`

## Execution Steps

### 1. Read This Plan
Review this file for the next incomplete task, key decisions, and information from previous agents.

### 2. Understand Your Task
Read your task file: `.tasks/ui-refactoring-tasks/task-XX-*.md`
- **Goal** — What you are trying to achieve
- **Key Points** — Important considerations
- **Done When** — Objective acceptance criteria

### 3. Execute the Task
- Make necessary code changes
- Ensure code compiles without errors
- Verify all Done When criteria are met

### 4. Update This Plan
- Mark the task as completed in `## Task Plan`
- Add a 1-2 sentence outcome summary in `## Shared Context`
- Document only critical decisions that affect future tasks

### 5. Await Approval (MANDATORY)
Wait for user confirmation before proceeding to the next task.

### 6. Review Task List (MANDATORY)
Analyze remaining tasks based on what you learned:
- Did you encounter unexpected complexity?
- Should any tasks be split, merged, removed, or reordered?
- Are there missing tasks?

### 7. Present Review Findings (MANDATORY)
Always present your findings — even if no changes are needed — and await user approval before proceeding.

### 8. Update Task Files (if approved)
- Modify/create task files as needed
- Update `## Task Plan` in PLAN.md accordingly

---

## Task Plan

- [x] [task-01-landing-page.md]: Landing page — публичная стартовая страница на `/`
- [x] [task-02-auth-redirects-and-navigation.md]: Редиректы аутентификации + навигация — `/cards` → `/sets`
- [x] [task-03-liquibase-study-migration.md]: Liquibase-миграция — новые колонки для MIXED-режима study
- [x] [task-04-models-and-enums.md]: Модели и enum-ы — MIXED в ExerciseType, Direction, новые поля
- [x] [task-05-study-service-refactor.md]: Рефакторинг StudyService — per-question type/direction, проверка ответов
- [x] [task-06-study-controller-refactor.md]: Рефакторинг StudyController — QuestionDto, убрать exerciseType param
- [x] [task-07-study-choose-ui.md]: UI выбора наборов — карточки вместо чекбоксов
- [x] [task-08-study-session-ui.md]: UI сессии изучения — MIXED mode, оба направления, flashcard
- [x] [task-09-study-results-ui.md]: UI результатов — таблица ошибок, обновлённые кнопки

---

## Shared Context

### Overview
Рефакторинг English Words App: убрать `/cards` как самостоятельный экран (слова живут только в контексте наборов `/sets/{id}`), создать landing page, полностью переработать study flow (MIXED-режим с рандомным типом упражнения и направлением перевода для каждого слова).

### Project Context
- **Архитектура**: Spring Boot 3.5, Thymeleaf + custom CSS (Inter, `#4f46e5`), PostgreSQL + Liquibase, Spring Security (OAuth2 + JWT)
- **`SecurityConfig.java`** — строки 82, 90: `defaultSuccessUrl("/cards")` → нужно менять на `/sets`; строка 62: `/` уже в `permitAll()`
- **`header.html`** — навбар: brand → `/cards`, "My Cards" → `/cards`, "Add Word", "Random" — всё на `/cards`
- **`WordCardController.java`** — 10+ мест с `redirect:/cards`
- **`StudySession`** — один `exerciseType` на всю сессию, `wordIdsOrder` (comma-separated IDs)
- **`StudyResult`** — нет полей `userAnswer` и `direction`
- **`StudyService.submitAnswer()`** — сравнивает только с `translation` (только EN→RU)
- **`study/choose.html`** — чекбоксы + radio для exerciseType
- **`study/session.html`** — один тип упражнения за сессию, только EN→RU
- **`study/results.html`** — нет таблицы ошибок с ответами пользователя, "Back to Cards"
- **CSS классы**: `.set-card`, `.sets-grid`, `.flashcard-container`, `.flashcard`, `.btn-primary`, `.progress-bar-container`, `.results-summary`
- **Liquibase**: 9 changeset-ов (001-009), master в `db.changelog-master.yaml`

### Key Decisions
- Study-сессия всегда MIXED — нет выбора типа упражнения пользователем
- Per-question метаданные хранятся в `exerciseTypesOrder` и `directionsOrder` (comma-separated, параллельно `wordIdsOrder`)
- Существующие колонки (`exercise_type` в `study_sessions`) остаются для обратной совместимости — новые сессии будут `MIXED`
- `Direction` — отдельный enum в `model/`, не вложенный
- `QuestionDto` — DTO в `dto/` с полной информацией о текущем вопросе (wordCard, exerciseType, direction, choices, indices)
- Нормализация ответов: `toLowerCase().trim().replaceAll("[.,!?;:]", "")`

### Caveats & Problems
- `WordCardController` остаётся (слова доступны из `/sets/{id}` контекста), но `GET /cards` должен редиректить на `/sets`
- `index.html` (шаблон для `/cards`) можно не удалять — он просто не будет использоваться после редиректа
- CSRF-токен обязателен во всех POST-формах в Thymeleaf-шаблонах
- `StudyResult.ExerciseType` — **не трогать** (отдельный enum от `StudySession.ExerciseType`), в result хранится конкретный тип (TEXT_INPUT/MULTIPLE_CHOICE), не MIXED
- `AnalyticsService.updateProgress()` уже вызывается из `StudyService.submitAnswer()` — не ломать эту интеграцию
