package com.example.b2b_opportunities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class TestUtils {

    public static boolean isDatabaseAvailable(String jdbcUrl, String username, String password) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static void waitForDatabase(String jdbcUrl, String username, String password, long timeout, TimeUnit timeUnit) throws InterruptedException {
        long endTime = System.currentTimeMillis() + timeUnit.toMillis(timeout);
        while (System.currentTimeMillis() < endTime) {
            if (isDatabaseAvailable(jdbcUrl, username, password)) {
                return;
            }
            Thread.sleep(1000); // Wait before retrying
        }
        throw new RuntimeException("Database is not available after waiting for " + timeout + " " + timeUnit.toString().toLowerCase());
    }
}

