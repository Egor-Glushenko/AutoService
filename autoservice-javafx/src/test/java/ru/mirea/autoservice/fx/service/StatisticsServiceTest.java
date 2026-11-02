package ru.mirea.autoservice.fx.service;

import org.junit.jupiter.api.Test;
import ru.mirea.autoservice.fx.model.OrderStatus;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StatisticsServiceTest {

    private static final LocalDate D = LocalDate.of(2026, 10, 1);

    private final StatisticsService service = new StatisticsService();

    @Test
    void calculatesIndicators() {
        OrderStatistics stats = service.calculate(List.of(
                TestOrders.order(1, OrderStatus.DONE, "1000", D, D, 1, "Белов"),
                TestOrders.order(2, OrderStatus.DONE, "3000", D, D.plusDays(2), 1, "Белов"),
                TestOrders.order(3, OrderStatus.CANCELLED, "0", D, D, 2, "Королёв"),
                TestOrders.order(4, OrderStatus.IN_PROGRESS, "500", D, null, 2, "Королёв"),
                TestOrders.order(5, OrderStatus.NEW, "0", D, null, 1, "Белов")));

        assertThat(stats.totalOrders()).isEqualTo(5);
        assertThat(stats.activeOrders()).isEqualTo(2);
        assertThat(stats.ordersByStatus()).containsEntry(OrderStatus.DONE, 2L).containsEntry(OrderStatus.NEW, 1L);
        assertThat(stats.completionRatePercent()).isEqualTo(200.0 / 3);
        assertThat(stats.revenue()).isEqualByComparingTo("4000");
        assertThat(stats.averageCheck()).isEqualByComparingTo("2000");
        assertThat(stats.minCost()).isEqualByComparingTo("1000");
        assertThat(stats.maxCost()).isEqualByComparingTo("3000");
        assertThat(stats.averageRepairDays()).isEqualTo(2.0);
        assertThat(stats.ordersByMechanic()).containsEntry("Белов", 3L).containsEntry("Королёв", 2L);
        assertThat(stats.topMechanic()).isEqualTo("Белов — 3");
    }

    @Test
    void handlesEmptyList() {
        OrderStatistics stats = service.calculate(List.of());

        assertThat(stats.totalOrders()).isZero();
        assertThat(stats.completionRatePercent()).isNull();
        assertThat(stats.averageCheck()).isNull();
        assertThat(stats.minCost()).isNull();
        assertThat(stats.averageRepairDays()).isNull();
        assertThat(stats.topMechanic()).isNull();
    }
}
