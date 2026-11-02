package ru.mirea.autoservice.fx.dao;

import ru.mirea.autoservice.fx.model.Mechanic;

import java.util.List;

public class MechanicDao {

    private static final String FIND_ALL =
            "SELECT id, full_name, specialization FROM mechanics ORDER BY full_name";
    private static final String EXISTS = "SELECT 1 FROM mechanics WHERE id = ?";

    private final ConnectionFactory connections;

    public MechanicDao(ConnectionFactory connections) {
        this.connections = connections;
    }

    public List<Mechanic> findAll() {
        return Jdbc.queryList(connections, FIND_ALL,
                rs -> new Mechanic(rs.getInt("id"), rs.getString("full_name"), rs.getString("specialization")),
                "загрузить список механиков");
    }

    public boolean existsById(int id) {
        return Jdbc.existsById(connections, EXISTS, id, "проверить механика");
    }
}
