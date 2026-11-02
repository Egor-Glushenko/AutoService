package ru.mirea.autoservice.console.service;

import org.junit.jupiter.api.Test;
import ru.mirea.autoservice.console.exception.BusinessException;
import ru.mirea.autoservice.console.exception.EntityNotFoundException;
import ru.mirea.autoservice.console.exception.FieldError;
import ru.mirea.autoservice.console.exception.ValidationException;
import ru.mirea.autoservice.console.model.Car;
import ru.mirea.autoservice.console.model.Mechanic;
import ru.mirea.autoservice.console.model.OrderData;
import ru.mirea.autoservice.console.model.OrderInput;
import ru.mirea.autoservice.console.model.OrderStatus;
import ru.mirea.autoservice.console.model.RepairOrder;
import ru.mirea.autoservice.console.repository.CarRepository;
import ru.mirea.autoservice.console.repository.MechanicRepository;
import ru.mirea.autoservice.console.repository.RepairOrderRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RepairOrderServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 29);

    private final FakeOrderRepository orders = new FakeOrderRepository();
    private final FakeCarRepository cars = new FakeCarRepository();
    private final FakeMechanicRepository mechanics = new FakeMechanicRepository();
    private final RepairOrderService service = new RepairOrderService(orders, cars, mechanics,
            new OrderValidator(Clock.fixed(TODAY.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC)));

    @Test
    void createsValidOrder() {
        assertThat(service.create(input(null, OrderStatus.NEW))).isEqualTo(42);
        assertThat(orders.inserted.orderNumber()).isEqualTo("ЗН-0031");
        assertThat(orders.inserted.carId()).isEqualTo(1);
    }

    @Test
    void suggestsNextNumber() {
        assertThat(service.suggestNextNumber()).isEqualTo("ЗН-0031");
    }

    @Test
    void rejectsDuplicateNumberAndExcludesOwnIdWhenEditing() {
        orders.numberExists = true;
        orders.stored = TestOrders.order(7, OrderStatus.NEW, "0", TODAY, null, 1, "Белов");

        assertThatThrownBy(() -> service.update(7, input(null, OrderStatus.NEW)))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.errors()).extracting(FieldError::field)
                                .contains(OrderValidator.NUMBER));
        assertThat(orders.lastExcludeId).isEqualTo(7);
    }

    @Test
    void rejectsSecondActiveOrderForCar() {
        orders.activeByCar = 1;

        assertFieldError(input(null, OrderStatus.IN_PROGRESS), OrderValidator.CAR);
    }

    @Test
    void rejectsOverloadedMechanic() {
        orders.activeByMechanic = RepairOrderService.MAX_ACTIVE_ORDERS_PER_MECHANIC;

        assertFieldError(input(null, OrderStatus.NEW), OrderValidator.MECHANIC);
    }

    @Test
    void closedOrderIgnoresActiveLimits() {
        orders.activeByCar = 1;
        orders.activeByMechanic = RepairOrderService.MAX_ACTIVE_ORDERS_PER_MECHANIC;

        OrderInput cancelled = new OrderInput(null, "ЗН-0031", 1, 1, "Замена масла",
                OrderStatus.CANCELLED, TODAY, TODAY, "0");
        assertThat(service.create(cancelled)).isEqualTo(42);
    }

    @Test
    void rejectsMissingCar() {
        cars.exists = false;

        assertFieldError(input(null, OrderStatus.NEW), OrderValidator.CAR);
    }

    @Test
    void updateOfDeletedOrderFails() {
        assertThatThrownBy(() -> service.update(7, input(null, OrderStatus.NEW)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void doneOrderCannotBeDeleted() {
        orders.stored = TestOrders.order(5, OrderStatus.DONE, "100", TODAY, TODAY, 2, "Белов");

        assertThatThrownBy(() -> service.delete(5)).isInstanceOf(BusinessException.class);
        assertThat(orders.deleted).isFalse();
    }

    @Test
    void deletesNotCompletedOrder() {
        orders.stored = TestOrders.order(5, OrderStatus.NEW, "0", TODAY, null, 2, "Белов");

        service.delete(5);

        assertThat(orders.deleted).isTrue();
    }

    @Test
    void reportsMissingOrderById() {
        assertThatThrownBy(() -> service.getRequired(99)).isInstanceOf(EntityNotFoundException.class);
    }

    private void assertFieldError(OrderInput input, String field) {
        assertThatThrownBy(() -> service.create(input))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.errors()).extracting(FieldError::field).contains(field));
    }

    private static OrderInput input(Integer id, OrderStatus status) {
        return new OrderInput(id, "ЗН-0031", 1, 1, "Замена масла и фильтров", status, TODAY, null, "0");
    }

    private static final class FakeOrderRepository implements RepairOrderRepository {

        boolean numberExists;
        int activeByCar;
        int activeByMechanic;
        Integer lastExcludeId;
        OrderData inserted;
        RepairOrder stored;
        boolean deleted;

        @Override
        public List<RepairOrder> findAll() {
            return List.of();
        }

        @Override
        public Optional<RepairOrder> findById(int id) {
            return Optional.ofNullable(stored);
        }

        @Override
        public int insert(OrderData order) {
            inserted = order;
            return 42;
        }

        @Override
        public boolean update(OrderData order) {
            return stored != null;
        }

        @Override
        public boolean delete(int id) {
            deleted = true;
            return true;
        }

        @Override
        public boolean existsByNumber(String orderNumber, Integer excludeId) {
            lastExcludeId = excludeId;
            return numberExists;
        }

        @Override
        public int countActiveByCar(int carId, Integer excludeId) {
            return activeByCar;
        }

        @Override
        public int countActiveByMechanic(int mechanicId, Integer excludeId) {
            return activeByMechanic;
        }

        @Override
        public int maxNumberSequence() {
            return 30;
        }
    }

    private static final class FakeCarRepository implements CarRepository {

        boolean exists = true;

        @Override
        public List<Car> findAll() {
            return List.of();
        }

        @Override
        public List<Car> findByClientId(int clientId) {
            return List.of();
        }

        @Override
        public Optional<Car> findById(int id) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(int id) {
            return exists;
        }

        @Override
        public int count() {
            return 1;
        }
    }

    private static final class FakeMechanicRepository implements MechanicRepository {

        @Override
        public List<Mechanic> findAll() {
            return List.of();
        }

        @Override
        public Optional<Mechanic> findById(int id) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(int id) {
            return true;
        }

        @Override
        public int count() {
            return 1;
        }
    }
}
