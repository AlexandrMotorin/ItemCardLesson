# Task 02: Редиректы аутентификации + навигация

**Type:** Code Modification

## Goal

Переключить все маршруты аутентификации и навигации с `/cards` на `/sets`, обновить header и ссылки во всех шаблонах.

## What to Do

- **SecurityConfig**: изменить `defaultSuccessUrl("/cards", true)` → `"/sets"` в oauth2Login и formLogin (строки 82, 90). Изменить `logoutSuccessUrl("/login?logout=true")` → `"/"` (строка 97)
- **Header (`fragments/header.html`)**:
  - Brand-ссылка `href="/cards"` → `href="/"` (строка 7)
  - "My Cards" `href="/cards"` → "My Sets" `href="/sets"` (строка 14, label строка 16)
  - Убрать ссылку "Add Word" `href="/cards/new"` (строки 22-25) и "Random" `href="/cards/random"` (строки 26-29) — они существуют только в контексте `/cards`
- **WordCardController**: все `redirect:/cards` → `redirect:/sets` (строки 111, 124, 144, 156, 168, 223, 238, 245)
- **study/choose.html**: Cancel `href="/cards"` → `href="/sets"` (строка 89)
- **study/results.html**: "Back to Cards" `href="/cards"` → "На главную" `href="/sets"` (строка 105)
- **AuthController**: `redirect:/login?registered` оставить как есть (уже ведёт на login → после login → `/sets`)

## Files/Areas

- `src/main/java/com/example/englishwordsapp/config/SecurityConfig.java:82,90,97` — **Изменить**: defaultSuccessUrl, logoutSuccessUrl
- `src/main/resources/templates/fragments/header.html:7,14-16,22-29` — **Изменить**: brand, навигационные ссылки
- `src/main/java/com/example/englishwordsapp/controller/WordCardController.java` — **Изменить**: все `redirect:/cards` → `redirect:/sets`
- `src/main/resources/templates/study/choose.html:89` — **Изменить**: Cancel ссылка
- `src/main/resources/templates/study/results.html:105` — **Изменить**: Back ссылка
- `src/main/resources/templates/index.html` — **Изменить**: ссылки `/cards/new` → `/sets`

## Key Points

- В SecurityConfig два места с `defaultSuccessUrl("/cards")` — oauth2Login (строка 82) и formLogin (строка 90)
- В header удалить ссылки "Add Word" и "Random" — они привязаны к `/cards` и не имеют смысла без отдельного экрана слов
- WordCardController остаётся функциональным (слова доступны из контекста наборов `/sets/{id}`), но его редиректы меняются
- `index.html` используется только для `/cards` — можно оставить, т.к. `/cards` будет редиректить (Task 04)

## Done When

- [ ] После OAuth2 login — редирект на `/sets`
- [ ] После form login — редирект на `/sets`
- [ ] После logout — редирект на `/` (landing page)
- [ ] Header содержит "My Sets" вместо "My Cards", brand ведёт на `/`
- [ ] Ссылки "Add Word" и "Random" удалены из header
- [ ] Все `redirect:/cards` в WordCardController заменены на `redirect:/sets`
- [ ] Cancel в study/choose.html и Back в study/results.html ведут на `/sets`
- [ ] Код компилируется без ошибок
