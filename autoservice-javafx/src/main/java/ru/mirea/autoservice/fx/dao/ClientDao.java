package ru.mirea.autoservice.fx.dao;

import ru.mirea.autoservice.fx.model.Client;

import java.util.List;

public class ClientDao {

    private static final String FIND_ALL = """
            SELECT cl.id, cl.full_name, cl.phone, cl.email,
                   (SELECT COUNT(*) FROM cars c WHERE c.client_id = cl.id) AS car_count,
                   (SELECT COUNT(*) FROM repair_orders o JOIN cars c ON c.id = o.car_id
                     WHERE c.client_id = cl.id) AS order_count
              FROM clients cl
             ORDER BY cl.full_name
            """;

    private final ConnectionFactory connections;

    public ClientDao(ConnectionFactory connections) {
        this.connections = connections;
    }

    public List<Client> findAll() {
        return Jdbc.queryList(connections, FIND_ALL, rs -> new Client(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getInt("car_count"),
                rs.getInt("order_count")), "загрузить список клиентов");
    }
}
