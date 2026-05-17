package com.example.englishwordsapp;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Базовый класс для интеграционных тестов, использующих
 * существующий PostgreSQL контейнер (через application-test.yml).
 * Все тесты, наследующие этот класс, используют реальную БД.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {
}