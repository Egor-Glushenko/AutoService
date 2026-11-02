package ru.mirea.autoservice.fx.service;

import ru.mirea.autoservice.fx.model.Car;
import ru.mirea.autoservice.fx.model.Mechanic;
import ru.mirea.autoservice.fx.model.OrderStatus;

import java.time.LocalDate;

public record OrderInput(
        Integer id,
        String orderNumber,
        Car car,
        Mechanic mechanic,
        String description,
        OrderStatus status,
        LocalDate createdDate,
        LocalDate completedDate,
        String costText) {
}
