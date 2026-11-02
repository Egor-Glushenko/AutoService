package ru.mirea.autoservice.fx.dao;

import ru.mirea.autoservice.fx.model.Car;

import java.util.List;

public class CarDao {

    private static final String FIND_ALL = """
            SELECT c.id, c.client_id, cl.full_name AS owner_name, c.brand, c.model, c.year, c.plate
              FROM cars c
              JOIN clients cl ON cl.id = c.client_id
             ORDER BY c.brand, c.model, c.plate
            """;
    private static final String EXISTS = "SELECT 1 FROM cars WHERE id = ?";

    private final ConnectionFactory connections;

    public CarDao(ConnectionFactory connections) {
        this.connections = connections;
    }

    public List<Car> findAll() {
        return Jdbc.queryList(connections, FIND_ALL, rs -> new Car(
                rs.getInt("id"),
                rs.getInt("client_id"),
                rs.getString("owner_name"),
                rs.getString("brand"),
                rs.getString("model"),
                rs.getInt("year"),
                rs.getString("plate")), "загрузить список автомобилей");
    }

    public boolean existsById(int id) {
        return Jdbc.existsById(connections, EXISTS, id, "проверить автомобиль");
    }
}
