# Backend Changes — Add Words to Sets

## 1. WordSetController — isOwner + isSubscribed

**Файл**: `src/main/java/com/example/englishwordsapp/controller/WordSetController.java`

В метод `viewSet` (GET `/sets/{id}`) добавлено:
- Вычисление `isOwner`: `wordSet.getOwner() != null && wordSet.getOwner().getId().equals(userId)`
- Передача `isOwner` в модель: `model.addAttribute("isOwner", isOwner)`
- `isSubscribed` уже передавался — оставлен без изменений

**WordSetService** — метод `isSubscribed(Long userId, Long setId)` уже существовал (строка 159), добавлять не пришлось.

## 2. WordCardController — preSelectedSetId + multi-set support

**Файл**: `src/main/java/com/example/englishwordsapp/controller/WordCardController.java`

### showAddCardForm (GET `/cards/new`):
- Добавлен параметр `@RequestParam(required = false) Long setId`
- Передаётся в модель как `preSelectedSetId`

### addCardToCollection (POST `/cards`):
- Добавлен параметр `@RequestParam(required = false) List<Long> setIds`
- Логика приоритетов:
  1. Если `setIds` не пуст → добавляет слово во все указанные наборы через `wordSetService.addWordToSet()`
  2. Иначе если `setId` задан → старая логика (один набор)
  3. Иначе → дефолтное добавление в коллекцию пользователя
- Для новых карточек (wordCard.getId() == null): после создания карточки также добавляет во все `setIds`
- Redirect: если ровно 1 setId → `redirect:/sets/{setId}`, иначе → `redirect:/cards`
