package ru.mirea.autoservice.console.repository;

import ru.mirea.autoservice.console.model.Car;

import java.util.List;
import java.util.Optional;

public interface CarRepository {

    List<Car> findAll();

    List<Car> findByClientId(int clientId);

    Optional<Car> findById(int id);

    boolean existsById(int id);

    int count();
}
