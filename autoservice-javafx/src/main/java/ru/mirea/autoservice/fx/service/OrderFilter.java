package ru.mirea.autoservice.fx.service;

import ru.mirea.autoservice.fx.model.OrderStatus;
import ru.mirea.autoservice.fx.model.RepairOrder;

import java.time.LocalDate;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Stream;

public record OrderFilter(String query, OrderStatus status, Integer mechanicId, LocalDate from, LocalDate to)
        implements Predicate<RepairOrder> {

    public static final OrderFilter NONE = new OrderFilter(null, null, null, null, null);

    @Override
    public boolean test(RepairOrder order) {
        if (status != null && order.status() != status) {
            return false;
        }
        if (mechanicId != null && order.mechanicId() != mechanicId) {
            return false;
        }
        if (from != null && order.createdDate().isBefore(from)) {
            return false;
        }
        if (to != null && order.createdDate().isAfter(to)) {
            return false;
        }
        return matchesQuery(order);
    }

    private boolean matchesQuery(RepairOrder order) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String needle = normalize(query);
        return Stream.of(order.orderNumber(), order.clientName(), order.clientPhone(), order.carTitle(),
                        order.plate(), order.mechanicName(), order.description())
                .anyMatch(value -> value != null && normalize(value).contains(needle));
    }

    private static String normalize(String value) {
        return value.strip().toLowerCase(Locale.ROOT).replace('ё', 'е');
    }
}
