# Fixes Applied — Review Bug Fixes

## Fix 1 (CRITICAL): addExistingForm не отправляет setIds
**Файл**: `src/main/resources/templates/fragments/card-form.html`
**Что сделано**: В функцию `selectWord()` добавлен код, который перед `addExistingForm.submit()` клонирует все hidden inputs с `name="setIds"` из `#hiddenSetInputs` в `addExistingForm`. Теперь при выборе существующего слова из поиска, выбранные наборы корректно передаются на сервер.

## Fix 2 (SECURITY): XSS через innerHTML
**Файл**: `src/main/resources/templates/fragments/card-form.html`
**Что сделано**: Заменено использование `div.innerHTML` с конкатенацией данных из API на безопасное создание DOM-элементов через `document.createElement()` + `textContent`. Теперь `word.englishWord`, `word.translation` и `word.difficultyLevel` вставляются как текст, а не как HTML.

## Fix 3 (WARNING): showEditCardForm — пустой userSets
**Файл**: `src/main/java/com/example/englishwordsapp/controller/WordCardController.java`
**Что сделано**: В метод `showEditCardForm` добавлена загрузка `userSets` через `wordSetService.getUserSets(userDetails.getId())` и передача в модель. Теперь при редактировании карточки селектор наборов заполнен.

## Fix 4 (WARNING): Hardcoded URLs → Thymeleaf
**Файлы**: `card-form.html`, `set-detail.html`
**Что сделано**:
- `set-detail.html`: `href="/sets"` → `th:href="@{/sets}"`
- `card-form.html`: `action="/cards"` → `th:action="@{/cards}"` для обеих форм (`addExistingForm` и `createNewForm`)
- `card-form.html`: `href="/cards"` → `th:href="@{/cards}"` для ссылок Cancel и Back to My Cards

## Fix 5 (WARNING): preSelectedSetId не сохраняется при ошибке валидации
**Файл**: `src/main/java/com/example/englishwordsapp/controller/WordCardController.java`
**Что сделано**: В блок обработки ошибок валидации метода `addCardToCollection` добавлена передача `preSelectedSetId` обратно в модель (`setIds.get(0)` или `null`). Теперь при перерисовке формы после ошибки валидации предвыбранный набор сохраняется.

## Компиляция
Проект успешно скомпилирован после всех изменений — ошибок нет.
