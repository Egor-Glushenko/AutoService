package ru.mirea.autoservice.console.repository.jdbc;

import ru.mirea.autoservice.console.model.Mechanic;
import ru.mirea.autoservice.console.repository.MechanicRepository;
import ru.mirea.autoservice.console.util.DatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class JdbcMechanicRepository implements MechanicRepository {

    private static final String FIND_ALL =
            "SELECT id, full_name, specialization FROM mechanics ORDER BY full_name";
    private static final String FIND_BY_ID =
            "SELECT id, full_name, specialization FROM mechanics WHERE id = ?";
    private static final String EXISTS = "SELECT 1 FROM mechanics WHERE id = ?";
    private static final String COUNT = "SELECT COUNT(*) FROM mechanics";

    private final DatabaseManager db;

    public JdbcMechanicRepository(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public List<Mechanic> findAll() {
        return JdbcSupport.queryList(db, FIND_ALL, JdbcMechanicRepository::map, "загрузить механиков");
    }

    @Override
    public Optional<Mechanic> findById(int id) {
        return JdbcSupport.queryOne(db, FIND_BY_ID, JdbcMechanicRepository::map, "найти механика", id);
    }

    @Override
    public boolean existsById(int id) {
        return JdbcSupport.exists(db, EXISTS, "проверить механика", id);
    }

    @Override
    public int count() {
        return JdbcSupport.queryOne(db, COUNT, rs -> rs.getInt(1), "посчитать механиков").orElse(0);
    }

    private static Mechanic map(ResultSet rs) throws SQLException {
        return new Mechanic(rs.getInt("id"), rs.getString("full_name"), rs.getString("specialization"));
    }
}
