package ru.mirea.autoservice.console.model;

import java.math.BigDecimal;
import java.util.Map;

public record OrderStatistics(
        int totalOrders,
        Map<OrderStatus, Long> ordersByStatus,
        long activeOrders,
        long doneOrders,
        long cancelledOrders,
        Double completionRatePercent,
        BigDecimal revenue,
        BigDecimal averageCheck,
        BigDecimal minCost,
        BigDecimal maxCost,
        Double averageRepairDays,
        int totalClients,
        int totalCars,
        int totalMechanics,
        Map<String, Long> ordersByMechanic,
        String topMechanic) {

    public long count(OrderStatus status) {
        return ordersByStatus.getOrDefault(status, 0L);
    }
}
