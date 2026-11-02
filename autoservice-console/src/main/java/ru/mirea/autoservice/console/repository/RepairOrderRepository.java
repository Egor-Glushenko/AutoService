package ru.mirea.autoservice.console.repository;

import ru.mirea.autoservice.console.model.OrderData;
import ru.mirea.autoservice.console.model.RepairOrder;

import java.util.List;
import java.util.Optional;

public interface RepairOrderRepository {

    List<RepairOrder> findAll();

    Optional<RepairOrder> findById(int id);

    int insert(OrderData order);

    boolean update(OrderData order);

    boolean delete(int id);

    boolean existsByNumber(String orderNumber, Integer excludeId);

    int countActiveByCar(int carId, Integer excludeId);

    int countActiveByMechanic(int mechanicId, Integer excludeId);

    int maxNumberSequence();
}
