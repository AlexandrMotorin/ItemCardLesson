# План: Добавление слов в наборы + UI-логика кнопок

## Задачи

### 1. WordSetController — передавать isOwner и isSubscribed в шаблон
**Файл**: `src/main/java/com/example/englishwordsapp/controller/WordSetController.java`
- В методе `viewSet`: добавить атрибуты модели `isOwner` и `isSubscribed`
- `isOwner = wordSet.getOwner() != null && wordSet.getOwner().getId().equals(userId)`
- `isSubscribed` — проверить через `WordSetService` (или репозиторий)

### 2. set-detail.html — условное отображение кнопок
**Файл**: `src/main/resources/templates/fragments/set-detail.html`
- Если `isOwner`:
  - Показывать кнопку "Add Word" (ссылка на `/cards/new?setId=${set.id}`)
  - НЕ показывать кнопки subscribe/unsubscribe и fork
- Если НЕ owner:
  - Если `isSubscribed` — показывать Unsubscribe + Fork
  - Если НЕ subscribed — показывать Subscribe + Fork
  - НЕ показывать кнопку "Add Word"

### 3. card-form.html — мультивыбор наборов с бейджами
**Файл**: `src/main/resources/templates/fragments/card-form.html`
- Добавить `<select>` для выбора набора из `userSets`
- При выборе из select — добавлять badge под dropdown
- Бейджи: inline стили, похожие на Bootstrap badge (цветные метки с крестиком для удаления)
- Скрытые `<input type="hidden" name="setIds" value="...">` для каждого выбранного набора
- Если `preSelectedSetId` передан — предвыбрать этот набор
- JavaScript: обработка выбора, добавления/удаления бейджей, управление hidden inputs

### 4. WordCardController — поддержка setId и множественных setIds
**Файл**: `src/main/java/com/example/englishwordsapp/controller/WordCardController.java`
- `showAddCardForm`: принимать `@RequestParam(required=false) Long setId`, передавать в модель как `preSelectedSetId`
- `addCardToCollection`: принимать `@RequestParam(required=false) List<Long> setIds` и добавлять слово во все выбранные наборы

### 5. CSS — стили для бейджей
**Файл**: `src/main/resources/static/css/main.css`
- Добавить стили `.set-badge` — inline badge с цветом, padding, border-radius, крестиком удаления
- Цвета: чередующиеся (accent, success, info, warning)

### 6. WordSetService — добавить метод isSubscribed (если нет)
**Файл**: `src/main/java/com/example/englishwordsapp/service/WordSetService.java`
- Проверить, есть ли метод для проверки подписки. Если нет — добавить.
