# Task 08: UI сессии изучения (study/session.html)

**Type:** Code Modification

## Goal

Полностью переработать шаблон сессии изучения: поддержка MIXED-режима (TEXT_INPUT и MULTIPLE_CHOICE в одной сессии), поддержка обоих направлений (EN_TO_RU, RU_TO_EN), показ правильного ответа при ошибке, flashcard с 3D-флипом.

## What to Do

- **Переписать `study/session.html`**:
  - **Прогресс-бар**: `X / Y слов` (из QuestionDto: currentIndex, totalWords)
  - **Карточка вопроса** с 3D-флипом:
    - Front: показывается слово на исходном языке (зависит от direction)
      - EN_TO_RU: показать `wordCard.englishWord`
      - RU_TO_EN: показать `wordCard.translation`
    - Индикатор direction: "EN → RU" или "RU → EN"
  - **TEXT_INPUT блок** (если `question.exerciseType == 'TEXT_INPUT'`):
    - Поле ввода для ответа
    - Кнопка "Проверить"
    - После проверки (через JS или redirect):
      - Правильно → зелёная подсветка, автопереход к следующему слову
      - Неправильно → красная подсветка, показать правильный ответ, кнопка "Далее"
  - **MULTIPLE_CHOICE блок** (если `question.exerciseType == 'MULTIPLE_CHOICE'`):
    - 4 кнопки с вариантами (из `question.choices`)
    - Текст кнопки зависит от direction:
      - EN_TO_RU: показывать `choice.translation`
      - RU_TO_EN: показывать `choice.englishWord`
    - При клике: подсветка выбранного (зелёный/красный), показ правильного ответа
  - **Hidden fields** в форме: `wordCardId`, `direction`, `exerciseType`
  - Badges: тип упражнения (✏️ Text Input / 🎯 Multiple Choice) + направление

- **JavaScript (inline)**:
  - Для MULTIPLE_CHOICE: при клике на вариант — submit формы с `answer = choice.id`
  - Показ результата ответа (correct/incorrect) перед переходом

- **CSS**: использовать `.flashcard-container`, `.flashcard`, `.flashcard-inner`, `.flashcard-front`, `.flashcard-back` из `card-detail.html` фрагмента

## Files/Areas

- `src/main/resources/templates/study/session.html` — **Переписать**: полная переработка
- `src/main/resources/static/css/main.css` — **Возможно дополнить**: стили для answer feedback (`.answer-correct`, `.answer-incorrect`)

## Key Points

- Модель передаёт `QuestionDto` (из Task 06): `question.wordCard`, `question.exerciseType`, `question.direction`, `question.choices`, `question.currentIndex`, `question.totalWords`
- Direction определяет какое поле wordCard показывать как вопрос и какое как ответ
- Для MULTIPLE_CHOICE варианты уже перемешаны в `StudyService.generateChoices()`
- Текущая версия использует server-side проверку (POST → redirect) — можно оставить этот подход, но добавить flash-сообщение с правильным ответом при ошибке
- Flashcard 3D-флип CSS уже есть в `main.css` — переиспользовать классы
- Не забыть CSRF-токен в форме

## Done When

- [ ] TEXT_INPUT и MULTIPLE_CHOICE корректно отображаются в одной сессии
- [ ] Направление перевода определяет показываемое слово (EN_TO_RU / RU_TO_EN)
- [ ] Прогресс-бар показывает текущую позицию
- [ ] При неправильном ответе показывается правильный ответ
- [ ] Flashcard стилизована с 3D-флипом
- [ ] Hidden fields direction и exerciseType передаются в POST
- [ ] Стили соответствуют дизайн-системе проекта
