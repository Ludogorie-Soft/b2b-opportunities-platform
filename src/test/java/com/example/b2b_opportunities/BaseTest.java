package com.example.b2b_opportunities;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Paths;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseTest {
    // This can be used for classes that don't require @Transactional
    private final static String hostPath = Paths.get("Deploy/icons").toAbsolutePath().toString();
    private final static String containerPath = "/icons";

    @Container
    protected static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withExposedPorts(5432) // waits for the port to be available
            .withFileSystemBind(hostPath, containerPath, BindMode.READ_ONLY);

    @DynamicPropertySource
    public static void overridePropertiesFile(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
