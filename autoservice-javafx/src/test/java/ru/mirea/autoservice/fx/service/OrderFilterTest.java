package ru.mirea.autoservice.fx.service;

import org.junit.jupiter.api.Test;
import ru.mirea.autoservice.fx.model.OrderStatus;
import ru.mirea.autoservice.fx.model.RepairOrder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class OrderFilterTest {

    private final RepairOrder order = TestOrders.order(7, OrderStatus.IN_PROGRESS, "1000",
            LocalDate.of(2026, 10, 10), null, 2, "Белов Игорь Станиславович");

    @Test
    void searchesByDifferentFieldsIgnoringCase() {
        assertThat(filter("зн-0007").test(order)).isTrue();
        assertThat(filter("ИВАНОВ").test(order)).isTrue();
        assertThat(filter("camry").test(order)).isTrue();
        assertThat(filter("а123").test(order)).isTrue();
        assertThat(filter("колодок").test(order)).isTrue();
        assertThat(filter("9161234501").test(order)).isTrue();
        assertThat(filter("Сидоров").test(order)).isFalse();
    }

    @Test
    void filtersByStatusMechanicAndPeriod() {
        assertThat(new OrderFilter(null, OrderStatus.IN_PROGRESS, 2, null, null).test(order)).isTrue();
        assertThat(new OrderFilter(null, OrderStatus.DONE, null, null, null).test(order)).isFalse();
        assertThat(new OrderFilter(null, null, 3, null, null).test(order)).isFalse();
        assertThat(new OrderFilter(null, null, null, LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 10))
                .test(order)).isTrue();
        assertThat(new OrderFilter(null, null, null, LocalDate.of(2026, 10, 11), null).test(order)).isFalse();
        assertThat(OrderFilter.NONE.test(order)).isTrue();
    }

    private static OrderFilter filter(String query) {
        return new OrderFilter(query, null, null, null, null);
    }
}
