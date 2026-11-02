package ru.mirea.autoservice.console.model;

import java.time.LocalDate;

public record OrderInput(
        Integer id,
        String orderNumber,
        Integer carId,
        Integer mechanicId,
        String description,
        OrderStatus status,
        LocalDate createdDate,
        LocalDate completedDate,
        String costText) {
}
