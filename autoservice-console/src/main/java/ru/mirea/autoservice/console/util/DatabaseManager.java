package ru.mirea.autoservice.console.util;

import ru.mirea.autoservice.console.config.DatabaseConfig;
import ru.mirea.autoservice.console.exception.DataAccessException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseManager {

    private final DatabaseConfig config;

    public DatabaseManager(DatabaseConfig config) {
        this.config = config;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.url(), config.user(), config.password());
    }

    public String describe() {
        return config.url();
    }

    public void checkConnection() {
        try (Connection connection = getConnection()) {
            if (!connection.isValid(5)) {
                throw new SQLException("Соединение с базой не прошло проверку");
            }
        } catch (SQLException e) {
            throw DataAccessException.of("подключиться к базе данных", e);
        }
    }
}
