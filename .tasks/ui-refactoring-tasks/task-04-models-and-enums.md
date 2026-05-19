# Task 04: Модели и enum-ы

**Type:** Code Modification

## Goal

Обновить JPA-сущности `StudySession` и `StudyResult` для поддержки MIXED-режима с per-question типом упражнения и направлением перевода.

## What to Do

- **`StudySession.java`**:
  - Добавить `MIXED` в enum `ExerciseType` (строка 22)
  - Добавить поле `exerciseTypesOrder` (String, `@Column(name = "exercise_types_order", columnDefinition = "TEXT")`) — comma-separated типы для каждого слова
  - Добавить поле `directionsOrder` (String, `@Column(name = "directions_order", columnDefinition = "TEXT")`) — comma-separated направления для каждого слова
- **`StudyResult.java`**:
  - Добавить поле `userAnswer` (String, `@Column(name = "user_answer", length = 500)`) — ответ пользователя
  - Добавить поле `direction` (String, `@Column(name = "direction", length = 20)`) — направление перевода
- **Создать enum `Direction.java`** в пакете `model/`:
  - `EN_TO_RU` — показывается englishWord, ответ — translation
  - `RU_TO_EN` — показывается translation, ответ — englishWord

## Files/Areas

- `src/main/java/com/example/englishwordsapp/model/StudySession.java:20-22` — **Изменить**: добавить MIXED в enum, добавить поля
- `src/main/java/com/example/englishwordsapp/model/StudyResult.java:44-52` — **Изменить**: добавить поля userAnswer, direction
- `src/main/java/com/example/englishwordsapp/model/Direction.java` — **Создать**: enum EN_TO_RU, RU_TO_EN

## Key Points

- Новые поля nullable — обратная совместимость с существующими данными в БД
- Не менять тип `exerciseType` в `StudySession` — он останётся enum, просто добавится значение `MIXED`
- `Direction` — отдельный enum (не вложенный), т.к. используется и в `StudySession`, и в `StudyResult`, и в `StudyService`
- `StudyResult.ExerciseType` уже содержит `TEXT_INPUT` и `MULTIPLE_CHOICE` — менять не нужно (в result по-прежнему хранится конкретный тип, а не MIXED)

## Done When

- [ ] `StudySession.ExerciseType` содержит `TEXT_INPUT`, `MULTIPLE_CHOICE`, `MIXED`
- [ ] `StudySession` имеет поля `exerciseTypesOrder` и `directionsOrder`
- [ ] `StudyResult` имеет поля `userAnswer` и `direction`
- [ ] Enum `Direction` создан с `EN_TO_RU`, `RU_TO_EN`
- [ ] Код компилируется без ошибок
