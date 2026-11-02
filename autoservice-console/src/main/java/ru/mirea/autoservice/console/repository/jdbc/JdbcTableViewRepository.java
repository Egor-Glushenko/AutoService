package ru.mirea.autoservice.console.repository.jdbc;

import ru.mirea.autoservice.console.model.TableSnapshot;
import ru.mirea.autoservice.console.repository.TableViewRepository;
import ru.mirea.autoservice.console.util.DatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcTableViewRepository implements TableViewRepository {

    private final DatabaseManager db;

    public JdbcTableViewRepository(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public TableSnapshot clients() {
        return new TableSnapshot("Таблица clients (клиенты)",
                List.of("id", "full_name", "phone", "email"),
                JdbcSupport.queryList(db,
                        "SELECT id, full_name, phone, email FROM clients ORDER BY id",
                        rs -> List.of(value(rs, "id"), value(rs, "full_name"), value(rs, "phone"), value(rs, "email")),
                        "вывести таблицу clients"));
    }

    @Override
    public TableSnapshot cars() {
        return new TableSnapshot("Таблица cars (автомобили)",
                List.of("id", "client_id", "brand", "model", "year", "plate"),
                JdbcSupport.queryList(db,
                        "SELECT id, client_id, brand, model, year, plate FROM cars ORDER BY id",
                        rs -> List.of(value(rs, "id"), value(rs, "client_id"), value(rs, "brand"),
                                value(rs, "model"), value(rs, "year"), value(rs, "plate")),
                        "вывести таблицу cars"));
    }

    @Override
    public TableSnapshot mechanics() {
        return new TableSnapshot("Таблица mechanics (механики)",
                List.of("id", "full_name", "specialization"),
                JdbcSupport.queryList(db,
                        "SELECT id, full_name, specialization FROM mechanics ORDER BY id",
                        rs -> List.of(value(rs, "id"), value(rs, "full_name"), value(rs, "specialization")),
                        "вывести таблицу mechanics"));
    }

    @Override
    public TableSnapshot repairOrders() {
        return new TableSnapshot("Таблица repair_orders (заказ-наряды)",
                List.of("id", "order_number", "car_id", "mechanic_id", "description", "status",
                        "created_date", "completed_date", "cost"),
                JdbcSupport.queryList(db,
                        "SELECT id, order_number, car_id, mechanic_id, description, status, "
                                + "created_date, completed_date, cost FROM repair_orders ORDER BY id",
                        JdbcTableViewRepository::orderRow,
                        "вывести таблицу repair_orders"));
    }

    private static List<String> orderRow(ResultSet rs) throws SQLException {
        return List.of(
                value(rs, "id"),
                value(rs, "order_number"),
                value(rs, "car_id"),
                value(rs, "mechanic_id"),
                value(rs, "description"),
                value(rs, "status"),
                value(rs, "created_date"),
                value(rs, "completed_date"),
                value(rs, "cost"));
    }

    private static String value(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? "" : value;
    }
}
