package ru.mirea.autoservice.console.repository.jdbc;

import ru.mirea.autoservice.console.exception.DataAccessException;
import ru.mirea.autoservice.console.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class JdbcSupport {

    @FunctionalInterface
    interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private JdbcSupport() {
    }

    static <T> List<T> queryList(DatabaseManager db, String sql, RowMapper<T> mapper, String action,
                                 Object... params) {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw DataAccessException.of(action, e);
        }
    }

    static <T> Optional<T> queryOne(DatabaseManager db, String sql, RowMapper<T> mapper, String action,
                                    Object... params) {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapper.map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw DataAccessException.of(action, e);
        }
    }

    static boolean exists(DatabaseManager db, String sql, String action, Object... params) {
        return queryOne(db, sql, rs -> Boolean.TRUE, action, params).isPresent();
    }

    static int update(DatabaseManager db, String sql, String action, Object... params) {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, params);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw DataAccessException.of(action, e);
        }
    }

    static int insert(DatabaseManager db, String sql, String action, Object... params) {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, new String[] {"id"})) {
            bind(statement, params);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("INSERT не вернул id новой записи");
                }
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw DataAccessException.of(action, e);
        }
    }

    private static void bind(PreparedStatement statement, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }
}
