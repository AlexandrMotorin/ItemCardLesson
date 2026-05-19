# Task 05: Рефакторинг StudyService

**Type:** Code Modification

## Goal

Переписать бизнес-логику `StudyService` для поддержки MIXED-режима: per-question тип упражнения (50/50), per-question направление перевода (50/50), корректная проверка ответов в обоих направлениях.

## What to Do

### `startSession()` (строки 37-73)
- Убрать параметр `exerciseTypeStr` — теперь всегда MIXED
- После сбора и перемешивания слов, для каждого слова случайно определить:
  - `exerciseType`: TEXT_INPUT или MULTIPLE_CHOICE (50/50, `Random`)
  - `direction`: EN_TO_RU или RU_TO_EN (50/50, `Random`)
- Сохранить в `exerciseTypesOrder` (comma-separated: "TEXT_INPUT,MULTIPLE_CHOICE,TEXT_INPUT,...")
- Сохранить в `directionsOrder` (comma-separated: "EN_TO_RU,RU_TO_EN,EN_TO_RU,...")
- Установить `session.setExerciseType(MIXED)`

### `getCurrentWord()` (строки 81-87)
- Оставить как есть, но добавить новый метод:
- **Создать `getCurrentQuestionInfo(Long sessionId)`** — возвращает QuestionDto с: wordCard, exerciseType, direction, currentIndex, totalWords, choices

### `submitAnswer()` (строки 104-151)
- Принимать direction как параметр
- Для TEXT_INPUT:
  - Если EN_TO_RU: сравнить userAnswer с `wordCard.getTranslation()`
  - Если RU_TO_EN: сравнить userAnswer с `wordCard.getEnglishWord()`
  - Нормализация: `toLowerCase().trim()`, удаление пунктуации (`.`, `,`, `!`, `?`)
- Для MULTIPLE_CHOICE:
  - Сравнить selectedOptionId с правильным wordCardId
- Сохранять `userAnswer` и `direction` в `StudyResult`

### `generateChoices()` (строки 153-182)
- Добавить параметр `direction`
- Если EN_TO_RU: варианты — это `translation` других слов
- Если RU_TO_EN: варианты — это `englishWord` других слов
- Возвращать List<WordCard> (данные одинаковые, но UI будет показывать нужное поле)

### Новые методы
- **`getExerciseTypeForIndex(StudySession session, int index)`** — парсинг `exerciseTypesOrder`
- **`getDirectionForIndex(StudySession session, int index)`** — парсинг `directionsOrder`
- **Создать DTO `QuestionDto`** (в пакете `dto/`): wordCard, exerciseType, direction, currentIndex, totalWords, choices (List<WordCard>, null для TEXT_INPUT)

## Files/Areas

- `src/main/java/com/example/englishwordsapp/service/StudyService.java` — **Переписать**: основная логика
- `src/main/java/com/example/englishwordsapp/dto/QuestionDto.java` — **Создать**: DTO для вопроса

## Key Points

- `Random` инстанс создавать один раз (поле класса или передавать) — не `new Random()` в каждом вызове
- Нормализация ответа: `answer.toLowerCase().trim().replaceAll("[.,!?;:]", "")` — одинаковая для обоих направлений
- При MULTIPLE_CHOICE генерировать 4 варианта: 1 правильный + 3 дистрактора из слов **текущей сессии** (по wordIdsOrder), а не из всех наборов
- `findWordSetForWord()` (строки 207-211) — оставить, но учесть что слово может принадлежать нескольким наборам
- Сигнатура `startSession` меняется: убрать `exerciseTypeStr`, оставить `(Long userId, List<Long> setIds)`
- Для обратной совместимости можно оставить старую сигнатуру с deprecation, но лучше обновить вызывающий код (StudyController — Task 06)

## Done When

- [ ] `startSession()` генерирует per-question exerciseType и direction (50/50)
- [ ] `submitAnswer()` корректно проверяет ответ в обоих направлениях (EN_TO_RU, RU_TO_EN)
- [ ] `generateChoices()` учитывает direction
- [ ] `QuestionDto` создан с полями: wordCard, exerciseType, direction, currentIndex, totalWords, choices
- [ ] Нормализация ответа: toLowerCase, trim, удаление пунктуации
- [ ] `StudyResult` сохраняет `userAnswer` и `direction`
- [ ] Код компилируется без ошибок
