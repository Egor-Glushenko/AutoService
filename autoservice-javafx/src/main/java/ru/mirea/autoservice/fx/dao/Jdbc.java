package ru.mirea.autoservice.fx.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

final class Jdbc {

    @FunctionalInterface
    interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private Jdbc() {
    }

    static <T> List<T> queryList(ConnectionFactory connections, String sql, RowMapper<T> mapper, String action) {
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapper.map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw DataAccessException.of(action, e);
        }
    }

    static boolean existsById(ConnectionFactory connections, String sql, int id, String action) {
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw DataAccessException.of(action, e);
        }
    }
}
