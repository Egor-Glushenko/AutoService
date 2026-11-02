package ru.mirea.autoservice.fx.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderData(
        Integer id,
        String orderNumber,
        int carId,
        int mechanicId,
        String description,
        OrderStatus status,
        LocalDate createdDate,
        LocalDate completedDate,
        BigDecimal cost) {
}
