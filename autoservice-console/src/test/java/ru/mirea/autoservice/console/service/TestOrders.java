package ru.mirea.autoservice.console.service;

import ru.mirea.autoservice.console.model.OrderStatus;
import ru.mirea.autoservice.console.model.RepairOrder;

import java.math.BigDecimal;
import java.time.LocalDate;

final class TestOrders {

    private TestOrders() {
    }

    static RepairOrder order(int id, OrderStatus status, String cost, LocalDate created, LocalDate completed,
                             int mechanicId, String mechanicName) {
        return order(id, status, cost, created, completed, "Иванов Иван Петрович", mechanicId, mechanicName);
    }

    static RepairOrder order(int id, OrderStatus status, String cost, LocalDate created, LocalDate completed,
                             String clientName, int mechanicId, String mechanicName) {
        return new RepairOrder(
                id,
                String.format("ЗН-%04d", id),
                1,
                "Toyota",
                "Camry",
                2018,
                "А123ВС77",
                clientName,
                "+79161234501",
                mechanicId,
                mechanicName,
                "Ходовая часть",
                "Замена тормозных колодок",
                status,
                created,
                completed,
                new BigDecimal(cost));
    }
}
