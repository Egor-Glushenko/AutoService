package ru.mirea.autoservice.fx.service;

import ru.mirea.autoservice.fx.model.OrderStatus;

import java.math.BigDecimal;
import java.util.Map;

public record OrderStatistics(
        int totalOrders,
        Map<OrderStatus, Long> ordersByStatus,
        long activeOrders,
        Double completionRatePercent,
        BigDecimal revenue,
        BigDecimal averageCheck,
        BigDecimal minCost,
        BigDecimal maxCost,
        Double averageRepairDays,
        Map<String, Long> ordersByMechanic,
        String topMechanic) {
}
