---
name: itemcard-fullstack
description: Development and improvement of English Words App (ItemCardLesson) — Spring Boot + Thymeleaf + Tailwind CSS + JavaScript. Creates clean, modern, and delightful user interfaces for learning English words through flashcards.
---

This skill is for working with the **English Words App** — a flashcard-based application for learning English vocabulary.

**Tech Stack:**
- **Backend**: Spring Boot (Java), Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf + Tailwind CSS + vanilla JavaScript
- **Build**: Gradle
- **Containerization**: Docker + docker-compose
- **Database**: H2 / PostgreSQL

## Core Objectives

1. **High-quality learning interface**  
   Build an effective, calm, and motivating learning experience for memorizing English words.

2. **Excellent flashcard UX**
    - Clear presentation of word, translation, example sentences, and difficulty level
    - Smooth navigation between list, view, edit, and study modes
    - Fully responsive design (mobile-first)

3. **Clean and maintainable code**
    - Follow project conventions and Tailwind best practices
    - Keep Thymeleaf templates semantic and readable
    - Use consistent utility class patterns
    - Write clean, well-commented JavaScript

## Design Principles (Frontend)

**Overall Aesthetic:**
- Modern minimalism focused on education
- Calm and pleasant color palette (soft blues, greens, neutrals with nice accents)
- Excellent spacing and typography
- Subtle micro-interactions and smooth transitions

**Tailwind Usage Guidelines:**
- Prefer utility-first approach
- Use consistent custom colors, spacing, and typography via `tailwind.config.js`
- Extract frequently repeated component classes into `@apply` in CSS files when it improves readability
- Maintain responsive design using Tailwind’s mobile-first breakpoints
- Keep HTML/Thymeleaf clean and not overly cluttered with classes

**Key Components:**
- **Flashcards** — should feel like real cards (with hover effects and optional 3D flip animation)
- **Difficulty badges** — clear visual distinction (Beginner, Intermediate, Advanced)
- **Forms** — clean, accessible, with good validation feedback
- **States** — loading, empty, success, and error states

**What to avoid:**
- Visual clutter
- Inconsistent spacing or colors
- Overly long utility class chains without extraction
- Tiny text or poor contrast

## Workflow

When receiving a task:

1. **Understand the requirement** thoroughly.
2. **Check existing styles** — review `tailwind.config.js`, common utilities, and component patterns.
3. **Design thoughtfully** — propose improvements to UX/UI when appropriate.
4. **Implement production-grade solutions**:
    - Semantic and accessible Thymeleaf markup
    - Responsive design with Tailwind
    - Good accessibility practices (contrast, ARIA where needed)
    - Clean vanilla JavaScript

## Priority Areas for Improvement

- Beautiful and interactive flashcards (including CSS 3D flip)
- Polish and consistency across all pages
- Study mode / learning session interface
- Dark mode support
- Overall navigation and user experience enhancements
- Performance and bundle size optimization (Tailwind purging)

**Golden Rule:** Every screen and component should feel like a high-quality educational product — polished, calm, and enjoyable to use.

Remember: This is a learning project that should be both **technically solid** and **visually delightful**.