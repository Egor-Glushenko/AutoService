package ru.mirea.autoservice.console.service;

import ru.mirea.autoservice.console.model.Mechanic;
import ru.mirea.autoservice.console.model.Car;
import ru.mirea.autoservice.console.model.Client;
import ru.mirea.autoservice.console.repository.CarRepository;
import ru.mirea.autoservice.console.repository.ClientRepository;
import ru.mirea.autoservice.console.repository.MechanicRepository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ReferenceService {

    private final ClientRepository clients;
    private final CarRepository cars;
    private final MechanicRepository mechanics;

    public ReferenceService(ClientRepository clients, CarRepository cars, MechanicRepository mechanics) {
        this.clients = clients;
        this.cars = cars;
        this.mechanics = mechanics;
    }

    public List<Client> clients() {
        return clients.findAll();
    }

    public List<Car> cars() {
        return cars.findAll();
    }

    public List<Mechanic> mechanics() {
        return mechanics.findAll();
    }

    public Optional<Client> client(int id) {
        return clients.findById(id);
    }

    public Optional<Car> car(int id) {
        return cars.findById(id);
    }

    public Optional<Mechanic> mechanic(int id) {
        return mechanics.findById(id);
    }

    public List<Car> carsOfClient(int clientId) {
        return cars.findByClientId(clientId);
    }

    public int clientCount() {
        return clients.count();
    }

    public int carCount() {
        return cars.count();
    }

    public int mechanicCount() {
        return mechanics.count();
    }

    public List<Client> findClients(String query) {
        String needle = normalize(query);
        return clients.findAll().stream()
                .filter(client -> normalize(client.fullName() + " " + client.phone()).contains(needle))
                .toList();
    }

    public List<Car> findCars(String query) {
        String needle = normalize(query);
        return cars.findAll().stream()
                .filter(car -> normalize(car.brand() + " " + car.model() + " " + car.plate() + " "
                        + car.ownerName()).contains(needle))
                .toList();
    }

    public List<Mechanic> findMechanics(String query) {
        String needle = normalize(query);
        return mechanics.findAll().stream()
                .filter(mechanic -> normalize(mechanic.fullName() + " " + mechanic.specialization())
                        .contains(needle))
                .toList();
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().toLowerCase(Locale.ROOT).replace('ё', 'е');
    }
}
