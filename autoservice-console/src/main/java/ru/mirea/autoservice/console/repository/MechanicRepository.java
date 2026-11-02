package ru.mirea.autoservice.console.repository;

import ru.mirea.autoservice.console.model.Mechanic;

import java.util.List;
import java.util.Optional;

public interface MechanicRepository {

    List<Mechanic> findAll();

    Optional<Mechanic> findById(int id);

    boolean existsById(int id);

    int count();
}
