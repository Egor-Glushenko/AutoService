package ru.mirea.autoservice.console.service;

import org.junit.jupiter.api.Test;
import ru.mirea.autoservice.console.model.OrderStatus;
import ru.mirea.autoservice.console.model.RepairOrder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderQueryServiceTest {

    private final OrderQueryService query = new OrderQueryService();

    private final List<RepairOrder> orders = List.of(
            TestOrders.order(1, OrderStatus.NEW, "1000", date(2026, 10, 1), null,
                    "Сидоров Алексей Викторович", 1, "Королёв Андрей"),
            TestOrders.order(2, OrderStatus.IN_PROGRESS, "5000", date(2026, 10, 5), null,
                    "Иванов Иван Петрович", 2, "Белов Игорь"),
            TestOrders.order(3, OrderStatus.DONE, "3000", date(2026, 10, 10), date(2026, 10, 12),
                    "Петрова Анна Сергеевна", 2, "Белов Игорь"),
            TestOrders.order(4, OrderStatus.CANCELLED, "0", date(2026, 10, 15), date(2026, 10, 15),
                    "Иванов Иван Петрович", 1, "Королёв Андрей"));

    @Test
    void searchesByClientIgnoringCase() {
        List<RepairOrder> found = query.search(orders, SearchField.CLIENT, "иВаНоВ");

        assertThat(found).extracting(RepairOrder::id).containsExactly(2, 4);
    }

    @Test
    void searchesByNumberCarAndDescription() {
        assertThat(query.search(orders, SearchField.NUMBER, "0003"))
                .extracting(RepairOrder::id).containsExactly(3);
        assertThat(query.search(orders, SearchField.CAR, "а123вс"))
                .extracting(RepairOrder::id).containsExactly(1, 2, 3, 4);
        assertThat(query.search(orders, SearchField.DESCRIPTION, "колодок"))
                .extracting(RepairOrder::id).containsExactly(1, 2, 3, 4);
    }

    @Test
    void filtersByStatusMechanicAndPeriod() {
        assertThat(query.filter(orders, OrderStatus.DONE, null, null, null))
                .extracting(RepairOrder::id).containsExactly(3);

        assertThat(query.filter(orders, null, 1, null, null))
                .extracting(RepairOrder::id).containsExactly(1, 4);

        assertThat(query.filter(orders, null, null, date(2026, 10, 5), date(2026, 10, 10)))
                .extracting(RepairOrder::id).containsExactly(2, 3);
    }

    @Test
    void filtersByClient() {
        assertThat(query.filterByClient(orders, "петрова"))
                .extracting(RepairOrder::id).containsExactly(3);
    }

    @Test
    void sortsByCostAndClient() {
        assertThat(query.sort(orders, OrderSortOption.COST_DESC))
                .extracting(RepairOrder::id).containsExactly(2, 3, 1, 4);

        assertThat(query.sort(orders, OrderSortOption.CREATED_ASC))
                .extracting(RepairOrder::id).containsExactly(1, 2, 3, 4);

        List<RepairOrder> byClient = query.sort(orders, OrderSortOption.CLIENT_ASC);
        assertThat(byClient.get(0).clientName()).startsWith("Иванов");
        assertThat(byClient.get(byClient.size() - 1).clientName()).startsWith("Сидоров");
    }

    private static LocalDate date(int year, int month, int day) {
        return LocalDate.of(year, month, day);
    }
}
