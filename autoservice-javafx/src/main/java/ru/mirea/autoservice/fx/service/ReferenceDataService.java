package ru.mirea.autoservice.fx.service;

import ru.mirea.autoservice.fx.dao.CarDao;
import ru.mirea.autoservice.fx.dao.ClientDao;
import ru.mirea.autoservice.fx.dao.MechanicDao;
import ru.mirea.autoservice.fx.model.Car;
import ru.mirea.autoservice.fx.model.Client;
import ru.mirea.autoservice.fx.model.Mechanic;

import java.util.List;

public class ReferenceDataService {

    private final ClientDao clientDao;
    private final CarDao carDao;
    private final MechanicDao mechanicDao;

    public ReferenceDataService(ClientDao clientDao, CarDao carDao, MechanicDao mechanicDao) {
        this.clientDao = clientDao;
        this.carDao = carDao;
        this.mechanicDao = mechanicDao;
    }

    public List<Client> clients() {
        return clientDao.findAll();
    }

    public List<Car> cars() {
        return carDao.findAll();
    }

    public List<Mechanic> mechanics() {
        return mechanicDao.findAll();
    }
}
