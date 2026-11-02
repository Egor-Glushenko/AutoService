package ru.mirea.autoservice.console.service;

import org.junit.jupiter.api.Test;
import ru.mirea.autoservice.console.model.OrderStatistics;
import ru.mirea.autoservice.console.model.OrderStatus;
import ru.mirea.autoservice.console.model.RepairOrder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class StatisticsServiceTest {

    private final StatisticsService service = new StatisticsService();

    private final List<RepairOrder> orders = List.of(
            TestOrders.order(1, OrderStatus.DONE, "1000", date(1), date(2), 1, "Белов Игорь"),
            TestOrders.order(2, OrderStatus.DONE, "3000", date(3), date(3), 1, "Белов Игорь"),
            TestOrders.order(3, OrderStatus.CANCELLED, "0", date(5), date(5), 2, "Королёв Андрей"),
            TestOrders.order(4, OrderStatus.NEW, "0", date(6), null, 2, "Королёв Андрей"),
            TestOrders.order(5, OrderStatus.IN_PROGRESS, "500", date(7), null, 3, "Тихонов Павел"));

    @Test
    void calculatesOrderMetrics() {
        OrderStatistics stats = service.calculate(orders, 8, 10, 5);

        assertThat(stats.totalOrders()).isEqualTo(5);
        assertThat(stats.activeOrders()).isEqualTo(2);
        assertThat(stats.doneOrders()).isEqualTo(2);
        assertThat(stats.cancelledOrders()).isEqualTo(1);
        assertThat(stats.completionRatePercent()).isCloseTo(66.67, within(0.01));
        assertThat(stats.revenue()).isEqualByComparingTo("4000");
        assertThat(stats.averageCheck()).isEqualByComparingTo("2000.00");
        assertThat(stats.minCost()).isEqualByComparingTo("1000");
        assertThat(stats.maxCost()).isEqualByComparingTo("3000");
        assertThat(stats.averageRepairDays()).isCloseTo(1.5, within(0.001));
        assertThat(stats.totalClients()).isEqualTo(8);
        assertThat(stats.totalCars()).isEqualTo(10);
        assertThat(stats.totalMechanics()).isEqualTo(5);
    }

    @Test
    void findsTopMechanicAndCountsByStatus() {
        OrderStatistics stats = service.calculate(orders, 8, 10, 5);

        assertThat(stats.topMechanic()).isEqualTo("Белов Игорь — 2");
        assertThat(stats.count(OrderStatus.DONE)).isEqualTo(2);
        assertThat(stats.count(OrderStatus.NEW)).isEqualTo(1);
    }

    @Test
    void returnsNoAverageForEmptySelection() {
        OrderStatistics stats = service.calculate(List.of(), 0, 0, 0);

        assertThat(stats.totalOrders()).isZero();
        assertThat(stats.revenue()).isEqualByComparingTo("0");
        assertThat(stats.averageCheck()).isNull();
        assertThat(stats.completionRatePercent()).isNull();
        assertThat(stats.averageRepairDays()).isNull();
        assertThat(stats.topMechanic()).isNull();
    }

    private static LocalDate date(int dayOfOctober) {
        return LocalDate.of(2026, 10, dayOfOctober);
    }
}
