# Task 07: UI выбора наборов (study/choose.html)

**Type:** Code Modification

## Goal

Переделать экран выбора наборов для изучения: заменить чекбоксы на карточки (`.set-card`), убрать выбор типа упражнения, добавить визуальное выделение выбранных наборов.

## What to Do

- **Переписать `study/choose.html`**:
  - Убрать секцию "Exercise Type" (radio-группа, строки 65-84)
  - Заменить секции "Subscribed Sets" и "Available Sets" с чекбоксами на `.sets-grid` с `.set-card` карточками
  - Каждая карточка содержит: название (`set.name`), описание (`set.description`), кол-во слов (`set.wordCards.size()`), кнопка/область клика для выбора
  - Выбранные карточки: добавляется CSS-класс `.set-card-selected` (border accent ��ветом `#4f46e5`, фоновая подсветка)
  - Скрытые `<input type="checkbox" name="setIds" value="${set.id}">` управляются через JavaScript
  - Кнопка "Начать изучение" (`btn-primary`) — disabled если ни один набор не выбран
  - Cancel ведёт на `/sets`
- **JavaScript (inline в шаблоне)**:
  - Клик по `.set-card` → toggle класс `.set-card-selected` + toggle hidden checkbox
  - Слушатель на изменение чекбоксов → обновление состояния кнопки "Начать"
- **CSS дополнения в `main.css`** (если нужны):
  - `.set-card-selected` — border: 2px solid var(--color-accent), background: rgba(79, 70, 229, 0.05)
  - `.set-card` в контексте study — cursor: pointer, transition

## Files/Areas

- `src/main/resources/templates/study/choose.html` — **Переписать**: полная переработка
- `src/main/resources/static/css/main.css` — **Дополнить**: `.set-card-selected` и hover-стили для интерактивных карточек

## Key Points

- Структура `.set-card` взята из `sets-list.html` (fragments/sets-list.html) — использовать те же классы
- Форма `POST /study/start` теперь отправляет только `setIds` (без `exerciseType`) — см. Task 06
- Для accessibility: карточки должны быть кликабельными через `<label>` или `role="checkbox"`
- CSRF-токен обязателен в форме (уже есть в текущей версии)
- Модель передаёт: `availableSets`, `subscribedSets` (без `exerciseTypes` — см. Task 06)

## Done When

- [ ] Наборы отображаются карточками (`.set-card`) вместо чекбоксов
- [ ] Клик по карточке переключает выбор с визуальным выделением
- [ ] Кнопка "Начать изучение" disabled без выбранных наборов
- [ ] Секция "Exercise Type" удалена
- [ ] Форма отправляет только `setIds`
- [ ] Стили соответствуют дизайн-системе проекта
