package ru.mirea.autoservice.fx.dao;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class DataAccessExceptionTest {

    @Test
    void translatesSqlStateToReadableMessage() {
        assertThat(message("08001")).contains("нет подключения");
        assertThat(message("28P01")).contains("нет подключения");
        assertThat(message("23505")).contains("уже существует");
        assertThat(message("23503")).contains("связанная запись");
        assertThat(message("23514")).contains("ограничения");
        assertThat(message("42P01")).contains("42P01");
        assertThat(message(null)).contains("без кода");
    }

    @Test
    void keepsOriginalExceptionAsCause() {
        SQLException cause = new SQLException("boom", "23505");
        assertThat(DataAccessException.of("сохранить", cause)).hasCause(cause)
                .hasMessageStartingWith("Не удалось сохранить:");
    }

    private static String message(String state) {
        return DataAccessException.of("загрузить", new SQLException("test", state)).getMessage();
    }
}
