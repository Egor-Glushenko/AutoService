package ru.mirea.autoservice.console.repository;

import ru.mirea.autoservice.console.model.Client;

import java.util.List;
import java.util.Optional;

public interface ClientRepository {

    List<Client> findAll();

    Optional<Client> findById(int id);

    int count();
}
