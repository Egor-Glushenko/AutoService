package ru.mirea.autoservice.console.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public record DatabaseConfig(String url, String user, String password) {

    private static final String RESOURCE = "/database.properties";

    public static DatabaseConfig load() {
        Properties properties = new Properties();
        try (InputStream in = DatabaseConfig.class.getResourceAsStream(RESOURCE)) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать " + RESOURCE, e);
        }
        String url = resolve(properties, "db.url", "DB_URL");
        String user = resolve(properties, "db.user", "DB_USER");
        String password = resolve(properties, "db.password", "DB_PASSWORD");
        if (url == null || user == null || password == null) {
            throw new IllegalStateException("Не заданы параметры подключения к БД: заполните db.url, db.user "
                    + "и db.password в database.properties или задайте DB_URL, DB_USER, DB_PASSWORD");
        }
        return new DatabaseConfig(url, user, password);
    }

    private static String resolve(Properties properties, String key, String envName) {
        String value = System.getProperty(key);
        if (isBlank(value)) {
            value = System.getenv(envName);
        }
        if (isBlank(value)) {
            value = properties.getProperty(key);
        }
        return isBlank(value) ? null : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Override
    public String toString() {
        return "DatabaseConfig[url=" + url + ", user=" + user + "]";
    }
}
