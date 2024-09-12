package com.example.b2b_opportunities;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseTest {

    @Container
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("b2b-test_db")
            .withUsername("postgres")
            .withPassword("password");

    @BeforeAll
    static void beforeAll() throws InterruptedException {
        postgres.start();
        // Wait for the database to be available
        TestUtils.waitForDatabase(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword(), 1, TimeUnit.MINUTES);

        System.setProperty("DB_URL", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgres.getUsername());
        System.setProperty("spring.datasource.password", postgres.getPassword());
    }
}
