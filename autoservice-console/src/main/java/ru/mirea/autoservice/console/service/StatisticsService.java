package ru.mirea.autoservice.console.service;

import ru.mirea.autoservice.console.model.OrderStatistics;
import ru.mirea.autoservice.console.model.OrderStatus;
import ru.mirea.autoservice.console.model.RepairOrder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class StatisticsService {

    public OrderStatistics calculate(List<RepairOrder> orders, int totalClients, int totalCars,
                                     int totalMechanics) {
        Map<OrderStatus, Long> byStatus = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) {
            byStatus.put(status, 0L);
        }
        orders.forEach(order -> byStatus.merge(order.status(), 1L, Long::sum));

        long active = orders.stream().filter(order -> order.status().isActive()).count();
        long done = byStatus.get(OrderStatus.DONE);
        long cancelled = byStatus.get(OrderStatus.CANCELLED);
        long closed = done + cancelled;

        Double completionRate = closed == 0 ? null : done * 100.0 / closed;

        List<RepairOrder> doneOrders = orders.stream()
                .filter(order -> order.status() == OrderStatus.DONE)
                .toList();
        List<BigDecimal> costs = doneOrders.stream().map(RepairOrder::cost).toList();
        BigDecimal revenue = costs.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageCheck = costs.isEmpty()
                ? null
                : revenue.divide(BigDecimal.valueOf(costs.size()), 2, RoundingMode.HALF_UP);
        BigDecimal minCost = costs.stream().min(Comparator.naturalOrder()).orElse(null);
        BigDecimal maxCost = costs.stream().max(Comparator.naturalOrder()).orElse(null);

        OptionalDouble averageDays = doneOrders.stream()
                .map(RepairOrder::repairDays)
                .filter(OptionalLong::isPresent)
                .mapToLong(OptionalLong::getAsLong)
                .average();

        Map<String, Long> byMechanic = orders.stream()
                .collect(Collectors.groupingBy(RepairOrder::mechanicName, TreeMap::new, Collectors.counting()));
        String topMechanic = byMechanic.entrySet().stream()
                .max(Map.Entry.<String, Long>comparingByValue()
                        .thenComparing(Map.Entry.comparingByKey(Comparator.reverseOrder())))
                .map(entry -> entry.getKey() + " — " + entry.getValue())
                .orElse(null);

        return new OrderStatistics(
                orders.size(),
                byStatus,
                active,
                done,
                cancelled,
                completionRate,
                revenue,
                averageCheck,
                minCost,
                maxCost,
                averageDays.isPresent() ? averageDays.getAsDouble() : null,
                totalClients,
                totalCars,
                totalMechanics,
                new LinkedHashMap<>(byMechanic),
                topMechanic);
    }
}
