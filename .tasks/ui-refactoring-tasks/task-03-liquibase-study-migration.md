# Task 03: Liquibase-миграция для Study

**Type:** Code Modification

## Goal

Добавить Liquibase-миграцию для новых колонок в `study_sessions` и `study_results`, необходимых для поддержки MIXED-режима (per-question exerciseType + direction).

## What to Do

- Создать файл `src/main/resources/db/changelog/changeset/010-study-mixed-mode.yaml`
- Добавить колонки в `study_sessions`:
  - `exercise_types_order` (TEXT, nullable) — comma-separated список типов упражнений для каждого слова (TEXT_INPUT,MULTIPLE_CHOICE,...)
  - `directions_order` (TEXT, nullable) — comma-separated список направлений (EN_TO_RU,RU_TO_EN,...)
- Добавить колонки в `study_results`:
  - `user_answer` (VARCHAR(500), nullable) — ответ пользователя
  - `direction` (VARCHAR(20), nullable) — направление перевода (EN_TO_RU / RU_TO_EN)
- Зарегистрировать миграцию в `db.changelog-master.yaml`

## Files/Areas

- `src/main/resources/db/changelog/changeset/010-study-mixed-mode.yaml` — **Создать**: changeset
- `src/main/resources/db/changelog/db.changelog-master.yaml` — **Изменить**: добавить include для 010

## Key Points

- Все новые колонки nullable — обратная совместимость с существующими данными
- Существующие сессии с `exercise_type = TEXT_INPUT / MULTIPLE_CHOICE` продолжат работать (null в новых полях)
- Формат: YAML, author: `english-words-app`, id: `010-study-mixed-mode`
- Колонка `exercise_type` в `study_sessions` уже существует (VARCHAR(20)) — её **не удаляем**, оставляем для обратной совместимости (будет `MIXED` для новых сессий)

## Done When

- [ ] Файл `010-study-mixed-mode.yaml` создан с корректным YAML-синтаксисом
- [ ] Миграция зарегистрирована в `db.changelog-master.yaml`
- [ ] Новые колонки: `exercise_types_order`, `directions_order` в `study_sessions`; `user_answer`, `direction` в `study_results`
- [ ] Все колонки nullable для обратной совместимости
