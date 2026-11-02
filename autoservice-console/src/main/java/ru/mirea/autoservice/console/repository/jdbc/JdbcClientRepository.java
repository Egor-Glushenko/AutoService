package ru.mirea.autoservice.console.repository.jdbc;

import ru.mirea.autoservice.console.model.Client;
import ru.mirea.autoservice.console.repository.ClientRepository;
import ru.mirea.autoservice.console.util.DatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class JdbcClientRepository implements ClientRepository {

    private static final String SELECT = """
            SELECT cl.id, cl.full_name, cl.phone, cl.email,
                   (SELECT COUNT(*) FROM cars c WHERE c.client_id = cl.id) AS car_count,
                   (SELECT COUNT(*) FROM repair_orders o JOIN cars c ON c.id = o.car_id
                     WHERE c.client_id = cl.id) AS order_count
              FROM clients cl
            """;
    private static final String FIND_ALL = SELECT + " ORDER BY cl.full_name";
    private static final String FIND_BY_ID = SELECT + " WHERE cl.id = ?";
    private static final String COUNT = "SELECT COUNT(*) FROM clients";

    private final DatabaseManager db;

    public JdbcClientRepository(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public List<Client> findAll() {
        return JdbcSupport.queryList(db, FIND_ALL, JdbcClientRepository::map, "загрузить клиентов");
    }

    @Override
    public Optional<Client> findById(int id) {
        return JdbcSupport.queryOne(db, FIND_BY_ID, JdbcClientRepository::map, "найти клиента", id);
    }

    @Override
    public int count() {
        return JdbcSupport.queryOne(db, COUNT, rs -> rs.getInt(1), "посчитать клиентов").orElse(0);
    }

    private static Client map(ResultSet rs) throws SQLException {
        return new Client(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getInt("car_count"),
                rs.getInt("order_count"));
    }
}
