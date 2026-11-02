package ru.mirea.autoservice.fx.dao;

import ru.mirea.autoservice.fx.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    private final DatabaseConfig config;

    public ConnectionFactory(DatabaseConfig config) {
        this.config = config;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.url(), config.user(), config.password());
    }

    public String describe() {
        return config.url();
    }
}
