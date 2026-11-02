package ru.mirea.autoservice.console.repository.jdbc;

import ru.mirea.autoservice.console.model.OrderData;
import ru.mirea.autoservice.console.model.OrderStatus;
import ru.mirea.autoservice.console.model.RepairOrder;
import ru.mirea.autoservice.console.repository.RepairOrderRepository;
import ru.mirea.autoservice.console.util.DatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class JdbcRepairOrderRepository implements RepairOrderRepository {

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

    private final DatabaseManager db;

    public JdbcRepairOrderRepository(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public List<RepairOrder> findAll() {
        return JdbcSupport.queryList(db, FIND_ALL, JdbcRepairOrderRepository::map, "загрузить заказ-наряды");
    }

    @Override
    public Optional<RepairOrder> findById(int id) {
        return JdbcSupport.queryOne(db, FIND_BY_ID, JdbcRepairOrderRepository::map, "найти заказ-наряд", id);
    }

    @Override
    public int insert(OrderData order) {
        return JdbcSupport.insert(db, INSERT, "сохранить заказ-наряд",
                order.orderNumber(), order.carId(), order.mechanicId(), order.description(),
                order.status().name(), order.createdDate(), order.completedDate(), order.cost());
    }

    @Override
    public boolean update(OrderData order) {
        return JdbcSupport.update(db, UPDATE, "обновить заказ-наряд",
                order.orderNumber(), order.carId(), order.mechanicId(), order.description(),
                order.status().name(), order.createdDate(), order.completedDate(), order.cost(),
                order.id()) == 1;
    }

    @Override
    public boolean delete(int id) {
        return JdbcSupport.update(db, DELETE, "удалить заказ-наряд", id) == 1;
    }

    @Override
    public boolean existsByNumber(String orderNumber, Integer excludeId) {
        return JdbcSupport.exists(db, EXISTS_NUMBER, "проверить номер заказ-наряда",
                orderNumber, excludeId == null ? NO_ID : excludeId);
    }

    @Override
    public int countActiveByCar(int carId, Integer excludeId) {
        return count(COUNT_ACTIVE_BY_CAR, carId, excludeId, "проверить заказы автомобиля");
    }

    @Override
    public int countActiveByMechanic(int mechanicId, Integer excludeId) {
        return count(COUNT_ACTIVE_BY_MECHANIC, mechanicId, excludeId, "проверить загрузку механика");
    }

    @Override
    public int maxNumberSequence() {
        return JdbcSupport.queryOne(db, MAX_NUMBER, rs -> rs.getInt(1), "получить следующий номер заказ-наряда")
                .orElse(0);
    }

    private int count(String sql, int ownerId, Integer excludeId, String action) {
        return JdbcSupport.queryOne(db, sql, rs -> rs.getInt(1), action,
                ownerId, excludeId == null ? NO_ID : excludeId).orElse(0);
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
