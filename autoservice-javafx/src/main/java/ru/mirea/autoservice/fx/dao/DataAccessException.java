package ru.mirea.autoservice.fx.dao;

import java.sql.SQLException;

public class DataAccessException extends RuntimeException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    static DataAccessException of(String action, SQLException e) {
        String state = e.getSQLState() == null ? "" : e.getSQLState();
        String reason;
        if (state.startsWith("08") || state.equals("28P01") || state.equals("3D000")) {
            reason = "нет подключения к базе данных. Проверьте, что PostgreSQL запущен "
                    + "(docker compose up -d) и параметры в database.properties верны.";
        } else if (state.equals("23505")) {
            reason = "запись с такими значениями уже существует.";
        } else if (state.equals("23503")) {
            reason = "связанная запись не найдена или используется в других данных.";
        } else if (state.equals("23514") || state.equals("23502")) {
            reason = "данные нарушают ограничения базы данных.";
        } else {
            reason = "ошибка базы данных (" + (state.isEmpty() ? "без кода" : state) + ").";
        }
        return new DataAccessException("Не удалось " + action + ": " + reason, e);
    }
}
