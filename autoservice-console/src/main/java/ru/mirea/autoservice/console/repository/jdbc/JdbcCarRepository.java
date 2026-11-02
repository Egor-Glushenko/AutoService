package ru.mirea.autoservice.console.repository.jdbc;

import ru.mirea.autoservice.console.model.Car;
import ru.mirea.autoservice.console.repository.CarRepository;
import ru.mirea.autoservice.console.util.DatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class JdbcCarRepository implements CarRepository {

    private static final String SELECT = """
            SELECT c.id, c.client_id, cl.full_name AS owner_name, c.brand, c.model, c.year, c.plate
              FROM cars c
              JOIN clients cl ON cl.id = c.client_id
            """;
    private static final String FIND_ALL = SELECT + " ORDER BY c.brand, c.model, c.plate";
    private static final String FIND_BY_CLIENT = SELECT + " WHERE c.client_id = ? ORDER BY c.brand, c.model";
    private static final String FIND_BY_ID = SELECT + " WHERE c.id = ?";
    private static final String EXISTS = "SELECT 1 FROM cars WHERE id = ?";
    private static final String COUNT = "SELECT COUNT(*) FROM cars";

    private final DatabaseManager db;

    public JdbcCarRepository(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public List<Car> findAll() {
        return JdbcSupport.queryList(db, FIND_ALL, JdbcCarRepository::map, "загрузить автомобили");
    }

    @Override
    public List<Car> findByClientId(int clientId) {
        return JdbcSupport.queryList(db, FIND_BY_CLIENT, JdbcCarRepository::map,
                "загрузить автомобили клиента", clientId);
    }

    @Override
    public Optional<Car> findById(int id) {
        return JdbcSupport.queryOne(db, FIND_BY_ID, JdbcCarRepository::map, "найти автомобиль", id);
    }

    @Override
    public boolean existsById(int id) {
        return JdbcSupport.exists(db, EXISTS, "проверить автомобиль", id);
    }

    @Override
    public int count() {
        return JdbcSupport.queryOne(db, COUNT, rs -> rs.getInt(1), "посчитать автомобили").orElse(0);
    }

    private static Car map(ResultSet rs) throws SQLException {
        return new Car(
                rs.getInt("id"),
                rs.getInt("client_id"),
                rs.getString("owner_name"),
                rs.getString("brand"),
                rs.getString("model"),
                rs.getInt("year"),
                rs.getString("plate"));
    }
}
