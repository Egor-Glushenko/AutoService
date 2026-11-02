package ru.mirea.autoservice.fx.dao;

import ru.mirea.autoservice.fx.model.OrderData;
import ru.mirea.autoservice.fx.model.OrderStatus;
import ru.mirea.autoservice.fx.model.RepairOrder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class RepairOrderDao {

    private static final String SELECT = """
            SELECT o.id, o.order_number, o.description, o.status, o.created_date, o.completed_date, o.cost,
                   c.id AS car_id, c.brand, c.model, c.year, c.plate,
                   cl.full_name AS client_name, cl.phone AS client_phone,
                   m.id AS mechanic_id, m.full_name AS mechanic_name, m.specialization
              FROM repair_orders o
              JOIN cars c ON c.id = o.car_id
              JOIN clients cl ON cl.id = c.client_id
              JOIN mechanics m ON m.id = o.mechanic_id
            """;
    private static final String FIND_ALL = SELECT + " ORDER BY o.created_date DESC, o.id DESC";
    private static final String FIND_BY_ID = SELECT + " WHERE o.id = ?";
    private static final String INSERT = """
            INSERT INTO repair_orders
                (order_number, car_id, mechanic_id, description, status, created_date, completed_date, cost)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE = """
            UPDATE repair_orders
               SET order_number = ?, car_id = ?, mechanic_id = ?, description = ?, status = ?,
                   created_date = ?, completed_date = ?, cost = ?
             WHERE id = ?
            """;
    private static final String DELETE = "DELETE FROM repair_orders WHERE id = ?";
    private static final String EXISTS_NUMBER =
            "SELECT 1 FROM repair_orders WHERE lower(order_number) = lower(?) AND id <> ?";
    private static final String COUNT_ACTIVE_BY_CAR =
            "SELECT COUNT(*) FROM repair_orders WHERE car_id = ? AND status IN ('NEW', 'IN_PROGRESS') AND id <> ?";
    private static final String COUNT_ACTIVE_BY_MECHANIC =
            "SELECT COUNT(*) FROM repair_orders WHERE mechanic_id = ? AND status IN ('NEW', 'IN_PROGRESS') AND id <> ?";
    private static final String MAX_NUMBER = """
            SELECT COALESCE(MAX(CAST(SUBSTRING(order_number FROM 4) AS INTEGER)), 0)
              FROM repair_orders
             WHERE order_number ~ '^ЗН-[0-9]{1,6}$'
            """;
    private static final int NO_ID = -1;

    private final ConnectionFactory connections;

    public RepairOrderDao(ConnectionFactory connections) {
        this.connections = connections;
    }

    public List<RepairOrder> findAll() {
        return Jdbc.queryList(connections, FIND_ALL, RepairOrderDao::map, "загрузить заказ-наряды");
    }

    public Optional<RepairOrder> findById(int id) {
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw DataAccessException.of("загрузить заказ-наряд", e);
        }
    }

    public int insert(OrderData order) {
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT, new String[] {"id"})) {
            bind(statement, order);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("INSERT не вернул id новой записи");
                }
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw DataAccessException.of("сохранить заказ-наряд", e);
        }
    }

    public boolean update(OrderData order) {
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            bind(statement, order);
            statement.setInt(9, order.id());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw DataAccessException.of("обновить заказ-наряд", e);
        }
    }

    public boolean delete(int id) {
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw DataAccessException.of("удалить заказ-наряд", e);
        }
    }

    public boolean existsByNumber(String orderNumber, Integer excludeId) {
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(EXISTS_NUMBER)) {
            statement.setString(1, orderNumber);
            statement.setInt(2, excludeId == null ? NO_ID : excludeId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw DataAccessException.of("проверить номер заказ-наряда", e);
        }
    }

    public int countActiveByCar(int carId, Integer excludeId) {
        return count(COUNT_ACTIVE_BY_CAR, carId, excludeId, "проверить заказы автомобиля");
    }

    public int countActiveByMechanic(int mechanicId, Integer excludeId) {
        return count(COUNT_ACTIVE_BY_MECHANIC, mechanicId, excludeId, "проверить загрузку механика");
    }

    public int findMaxNumberSequence() {
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(MAX_NUMBER);
             ResultSet rs = statement.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw DataAccessException.of("получить следующий номер заказ-наряда", e);
        }
    }

    private int count(String sql, int ownerId, Integer excludeId, String action) {
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ownerId);
            statement.setInt(2, excludeId == null ? NO_ID : excludeId);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw DataAccessException.of(action, e);
        }
    }

    private static void bind(PreparedStatement statement, OrderData order) throws SQLException {
        statement.setString(1, order.orderNumber());
        statement.setInt(2, order.carId());
        statement.setInt(3, order.mechanicId());
        statement.setString(4, order.description());
        statement.setString(5, order.status().name());
        statement.setObject(6, order.createdDate());
        statement.setObject(7, order.completedDate(), Types.DATE);
        statement.setBigDecimal(8, order.cost());
    }

    private static RepairOrder map(ResultSet rs) throws SQLException {
        return new RepairOrder(
                rs.getInt("id"),
                rs.getString("order_number"),
                rs.getInt("car_id"),
                rs.getString("brand"),
                rs.getString("model"),
                rs.getInt("year"),
                rs.getString("plate"),
                rs.getString("client_name"),
                rs.getString("client_phone"),
                rs.getInt("mechanic_id"),
                rs.getString("mechanic_name"),
                rs.getString("specialization"),
                rs.getString("description"),
                OrderStatus.valueOf(rs.getString("status")),
                rs.getObject("created_date", LocalDate.class),
                rs.getObject("completed_date", LocalDate.class),
                rs.getBigDecimal("cost"));
    }
}
