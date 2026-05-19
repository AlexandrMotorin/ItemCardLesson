# Task 06: Рефакторинг StudyController

**Type:** Code Modification

## Goal

Обновить `StudyController` для работы с MIXED-режимом: убрать выбор типа упражнения, использовать `QuestionDto`, передавать direction в шаблоны, обновить логику результатов.

## What to Do

### `chooseSets()` (GET /study)
- Убрать `model.addAttribute("exerciseTypes", ...)` — тип упражнения больше не выбирается

### `startSession()` (POST /study/start)
- Убрать параметр `@RequestParam("exerciseType")`
- Вызывать `studyService.startSession(userId, setIds)` без exerciseType

### `showSession()` (GET /study/session/{id})
- Вместо ручного получения `currentWord` и `choices` — вызвать `studyService.getCurrentQuestionInfo(id)`
- Передавать в модель `QuestionDto`: question.wordCard, question.exerciseType, question.direction, question.choices, question.currentIndex, question.totalWords

### `submitAnswer()` (POST /study/session/{id}/answer)
- Добавить параметр `@RequestParam("direction")`
- Передавать direction в `studyService.submitAnswer()`
- Убрать получение exerciseType из request — брать из сессии (текущий index)

### `showResults()` (GET /study/session/{id}/results)
- Добавить в модель `incorrectResults` — только неправильные ответы для таблицы ошибок
- Каждый incorrect result должен содержать: wordCard, userAnswer, correctAnswer, direction

## Files/Areas

- `src/main/java/com/example/englishwordsapp/controller/StudyController.java` — **Переписать**: все методы

## Key Points

- `StudyController` больше не создаёт choices самостоятельно — это делает `StudyService.getCurrentQuestionInfo()`
- Direction передаётся как hidden field в форме сессии → обратно в submitAnswer
- Для таблицы ошибок в results: создать вспомогательный DTO или использовать Map — `incorrectResults` содержит данные о неправильных ответах с userAnswer
- `nextWord()` endpoint (GET /study/session/{id}/next) — пересмотреть: если submitAnswer автоматически продвигает index, этот endpoint может быть не нужен (оставить для skip)

## Done When

- [ ] POST /study/start не требует параметр exerciseType
- [ ] GET /study/session/{id} передаёт QuestionDto с exerciseType, direction, choices
- [ ] POST /study/session/{id}/answer принимает и передаёт direction
- [ ] GET /study/session/{id}/results содержит incorrectResults для таблицы ошибок
- [ ] Код компилируется без ошибок
