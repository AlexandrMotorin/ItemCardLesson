# Task 01: Landing Page

**Type:** Code Modification

## Goal

Создать публичную стартовую страницу на корневом маршруте `/`, которая представляет сервис English Words App и направляет пользователя к регистрации/входу или к наборам (если авторизован).

## What to Do

- Создать `HomeController.java` в `controller/` с `GET /` — возвращает шаблон `landing`
- Создать шаблон `landing.html` в `templates/` с layout-декоратором:
  - Баннер с названием сервиса "EnglishCards" и слоганом
  - 3-4 карточки преимуществ (изучение по наборам, два типа упражнений, рандомный порядок, статистика прогресса)
  - Кнопки "Зарегистрироваться" → `/register`, "Войти" → `/login`
  - Thymeleaf-условие `sec:authorize="isAuthenticated()"` → показать кнопку "Перейти к наборам" → `/sets` вместо login/register
- Убедиться что SecurityConfig уже имеет `"/"` в `permitAll()` (сейчас есть — строка 62)
- Использовать существующие CSS-классы: `.hero`, `.btn-primary`, `.btn-ghost`, `.card`, `.container`
- Стилизация: шрифт Inter, основной цвет `#4f46e5`, карточки с тенями

## Files/Areas

- `src/main/java/com/example/englishwordsapp/controller/HomeController.java` — **Создать**: контроллер с `GET /`
- `src/main/resources/templates/landing.html` — **Создать**: Thymeleaf-шаблон landing page
- `src/main/resources/static/css/main.css` — **Возможно дополнить**: CSS для landing-специфичных стилей (`.landing-features`, `.feature-card`)
- `src/main/java/com/example/englishwordsapp/config/SecurityConfig.java:62` — **Проверить**: `/` уже в `permitAll()`

## Key Points

- `/` уже имеет `permitAll()` в SecurityConfig — отдельного изменения не требуется
- Шаблон должен использовать `layout:decorate="layout"` как все остальные страницы проекта
- Для проверки авторизации использовать `xmlns:sec="http://www.thymeleaf.org/extras/spring-security"` (как в `header.html`)
- Не трогать header-фрагмент — он обновится в Task 02

## Done When

- [ ] `GET /` возвращает landing page без аутентификации (HTTP 200)
- [ ] Авторизованный попадает сразу на страницу с наборами карточек (/sets). Если у него их ещё нет то, то показывать пустую страницу с кнопкой "Создать первый набор" → `/sets/create`.
- [ ] Страница использует layout-декоратор и стилизована в едином стиле проекта
- [ ] Код компилируется без ошибок
