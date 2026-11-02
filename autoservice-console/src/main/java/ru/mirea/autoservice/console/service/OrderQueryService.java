package ru.mirea.autoservice.console.service;

import ru.mirea.autoservice.console.model.OrderStatus;
import ru.mirea.autoservice.console.model.RepairOrder;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class OrderQueryService {

    public List<RepairOrder> search(List<RepairOrder> source, SearchField field, String query) {
        String needle = normalize(query);
        return source.stream()
                .filter(order -> normalize(field.text(order)).contains(needle))
                .toList();
    }

    public List<RepairOrder> filter(List<RepairOrder> source, OrderStatus status, Integer mechanicId,
                                    LocalDate from, LocalDate to) {
        return source.stream()
                .filter(order -> status == null || order.status() == status)
                .filter(order -> mechanicId == null || order.mechanicId() == mechanicId)
                .filter(order -> from == null || !order.createdDate().isBefore(from))
                .filter(order -> to == null || !order.createdDate().isAfter(to))
                .toList();
    }

    public List<RepairOrder> filterByClient(List<RepairOrder> source, String clientQuery) {
        String needle = normalize(clientQuery);
        return source.stream()
                .filter(order -> normalize(order.clientName()).contains(needle))
                .toList();
    }

    public List<RepairOrder> sort(List<RepairOrder> source, OrderSortOption option) {
        return source.stream().sorted(option.comparator()).toList();
    }

    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().toLowerCase(Locale.ROOT).replace('ё', 'е');
    }
}
