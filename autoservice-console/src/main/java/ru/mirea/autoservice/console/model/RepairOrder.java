package ru.mirea.autoservice.console.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.OptionalLong;

public record RepairOrder(
        int id,
        String orderNumber,
        int carId,
        String carBrand,
        String carModel,
        int carYear,
        String plate,
        String clientName,
        String clientPhone,
        int mechanicId,
        String mechanicName,
        String mechanicSpecialization,
        String description,
        OrderStatus status,
        LocalDate createdDate,
        LocalDate completedDate,
        BigDecimal cost) {

    public String carTitle() {
        return carBrand + " " + carModel;
    }

    public OptionalLong repairDays() {
        if (completedDate == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(ChronoUnit.DAYS.between(createdDate, completedDate) + 1);
    }
}
