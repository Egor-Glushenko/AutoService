package ru.mirea.autoservice.console.repository;

import ru.mirea.autoservice.console.model.TableSnapshot;

public interface TableViewRepository {

    TableSnapshot clients();

    TableSnapshot cars();

    TableSnapshot mechanics();

    TableSnapshot repairOrders();
}
