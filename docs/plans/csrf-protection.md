# Поддержка CSRF-токенов в формах и AJAX-запросах

## Overview

Добавить поддержку CSRF-токенов для всех Thymeleaf-форм, выполняющих изменяющие запросы (POST), и для JavaScript fetch-запросов. REST API (`/api/**`) остаётся без изменений — CSRF для него уже отключён через `ignoringRequestMatchers`.

**Текущее состояние:**
- CSRF **включён** в Spring Security (по умолчанию)
- REST API (`/api/**`) исключён из CSRF через `ignoringRequestMatchers("/api/**")`
- Ни одна Thymeleaf-форма **не содержит** скрытого поля CSRF-токена
- JavaScript fetch-запросы **не отправляют** CSRF-заголовок

**Проблема:** Без CSRF-токена все POST-запросы из браузера будут отклоняться Spring Security с ошибкой 403.

## Context (from discovery)

**Файлы, содержащие формы с POST-запросами:**

1. [`src/main/resources/templates/index.html`](src/main/resources/templates/index.html) — форма удаления карточки (`POST /cards/{id}/remove`)
2. [`src/main/resources/templates/fragments/card-form.html`](src/main/resources/templates/fragments/card-form.html) — две формы: добавление существующего слова и создание нового (`POST /cards`)
3. [`src/main/resources/templates/fragments/card-detail.html`](src/main/resources/templates/fragments/card-detail.html) — три формы: обновление статуса (`POST /cards/{id}/status`), удаление из коллекции (`POST /cards/{id}/remove`), добавление в коллекцию (`POST /cards/{id}/add`)
4. [`src/main/resources/templates/layout.html`](src/main/resources/templates/layout.html) — базовый layout, куда добавить CSRF meta-тег

**Файлы с JavaScript-кодом, использующим fetch:**
1. [`src/main/resources/templates/fragments/card-form.html`](src/main/resources/templates/fragments/card-form.html) — fetch для поиска слов (`/cards/search-global`). Это GET-запрос, но для будущей совместимости добавим CSRF-заголовок глобально.

**Security:**
- [`src/main/java/com/example/englishwordsapp/config/SecurityConfig.java`](src/main/java/com/example/englishwordsapp/config/SecurityConfig.java) — CSRF уже настроен с исключением для `/api/**`. Менять не требуется.

## Development Approach

- **testing approach**: без тестов (по запросу)
- Подход очевиден — один путь реализации, варианты не предлагаются
- Каждая задача завершается полностью перед переходом к следующей

## Solution Overview

**Механизм работы CSRF в Spring Security + Thymeleaf:**

1. Spring Security автоматически генерирует CSRF-токен для каждой сессии
2. Thymeleaf предоставляет доступ к токену через `${_csrf}` — объект типа `CsrfToken`
3. Для форм: `<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />`
4. Для AJAX: CSRF-токен передаётся через HTTP-заголовок `X-CSRF-TOKEN`
5. Значение токена берётся из meta-тега в `<head>` (рендерится Thymeleaf)

**Ключевые решения:**
- CSRF-токен будет доступен через `<meta name="_csrf" th:content="${_csrf.token}">` и `<meta name="_csrf_header" th:content="${_csrf.headerName}">` в layout.html
- JavaScript будет читать эти meta-теги и добавлять заголовок `X-CSRF-TOKEN` во все fetch-запросы глобально
- Для исключения GET-запросов от CSRF-заголовка — используем проверку метода

## Technical Details

### Список всех форм для модификации:

| Файл | Форма | Метод | URL |
|------|-------|-------|-----|
| `index.html:62` | Remove card | POST | `/cards/{id}/remove` |
| `card-form.html:143` | Add existing word | POST | `/cards` |
| `card-form.html:153` | Create new word | POST | `/cards` |
| `card-detail.html:81` | Update status | POST | `/cards/{id}/status` |
| `card-detail.html:94` | Remove from collection | POST | `/cards/{id}/remove` |
| `card-detail.html:103` | Add to collection | POST | `/cards/{id}/add` |

### Поток обработки CSRF:

```
Браузер → GET /cards → Сервер рендерит HTML с CSRF-токеном в форме и meta-теге
Браузер → POST /cards (с _csrf в теле) → Сервер валидирует токен → 200/302
Браузер → fetch('POST /cards/...') с X-CSRF-TOKEN → Сервер валидирует токен → 200
```

## Implementation Steps

### Task 1: Добавить CSRF meta-теги в layout.html

**Files:**
- Modify: [`src/main/resources/templates/layout.html`](src/main/resources/templates/layout.html)

- [ ] Добавить `<meta name="_csrf" th:content="${_csrf.token}">` в `<head>` layout.html
- [ ] Добавить `<meta name="_csrf_header" th:content="${_csrf.headerName}">` в `<head>` layout.html
- [ ] Добавить `<meta name="_csrf_parameter" th:content="${_csrf.parameterName}">` в `<head>` layout.html

### Task 2: Добавить CSRF-токен во все Thymeleaf-формы в index.html

**Files:**
- Modify: [`src/main/resources/templates/index.html`](src/main/resources/templates/index.html)

- [ ] В форму удаления (строка 62) добавить скрытое поле с CSRF-токеном

### Task 3: Добавить CSRF-токен во все Thymeleaf-формы в card-form.html

**Files:**
- Modify: [`src/main/resources/templates/fragments/card-form.html`](src/main/resources/templates/fragments/card-form.html)

- [ ] В форму `addExistingForm` (строка 143) добавить скрытое поле с CSRF-токеном
- [ ] В форму `createNewForm` (строка 153) добавить скрытое поле с CSRF-токеном

### Task 4: Добавить CSRF-токен во все Thymeleaf-формы в card-detail.html

**Files:**
- Modify: [`src/main/resources/templates/fragments/card-detail.html`](src/main/resources/templates/fragments/card-detail.html)

- [ ] В форму обновления статуса (строка 81) добавить скрытое поле с CSRF-токеном
- [ ] В форму удаления из коллекции (строка 94) добавить скрытое поле с CSRF-токеном
- [ ] В форму добавления в коллекцию (строка 103) добавить скрытое поле с CSRF-токеном

### Task 5: Добавить глобальный JS-обработчик CSRF для fetch-запросов

**Files:**
- Modify: [`src/main/resources/templates/layout.html`](src/main/resources/templates/layout.html)

- [ ] Добавить JS-код в layout.html (перед закрывающим `</body>`), который:
  - Читает CSRF-токен и имя заголовка из meta-тегов
  - Создаёт функцию-обёртку или monkey-patch для `window.fetch`, добавляющую заголовок `X-CSRF-TOKEN` для всех non-GET запросов
  - Исключает запросы к `/api/**` от CSRF-заголовка

### Task 6: Проверить конфигурацию SecurityConfig

**Files:**
- Read: [`src/main/java/com/example/englishwordsapp/config/SecurityConfig.java`](src/main/java/com/example/englishwordsapp/config/SecurityConfig.java)

- [ ] Убедиться, что `.ignoringRequestMatchers("/api/**")` остаётся без изменений
- [ ] Убедиться, что CSRF защита включена для всех остальных путей (по умолчанию)

### Task 7: Verify acceptance criteria

- [ ] Запустить приложение и проверить, что страницы загружаются без ошибок CSRF
- [ ] Проверить отправку каждой формы (добавление, удаление, обновление статуса) — должен быть 302/200, не 403
- [ ] Проверить AJAX-поиск `/cards/search-global` — GET-запросы не должны требовать CSRF
- [ ] Проверить, что REST API (`/api/cards/**`) продолжает работать без CSRF
- [ ] Запустить сборку: `./gradlew build`

## Post-Completion

**Manual verification:**
- Открыть приложение в браузере, выполнить логин
- Проверить форму добавления слова (создание нового и из существующих)
- Проверить форму обновления статуса на странице деталей карточки
- Проверить форму удаления слова из коллекции (на index.html и card-detail.html)
- Проверить AJAX-поиск в форме добавления слова
- Проверить что `/api/**` эндпоинты работают (через Postman/curl с JWT-токеном)
