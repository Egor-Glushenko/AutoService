package ru.mirea.autoservice.fx.service;

import org.junit.jupiter.api.Test;
import ru.mirea.autoservice.fx.dao.CarDao;
import ru.mirea.autoservice.fx.dao.MechanicDao;
import ru.mirea.autoservice.fx.dao.RepairOrderDao;
import ru.mirea.autoservice.fx.model.Car;
import ru.mirea.autoservice.fx.model.Mechanic;
import ru.mirea.autoservice.fx.model.OrderData;
import ru.mirea.autoservice.fx.model.OrderStatus;
import ru.mirea.autoservice.fx.model.RepairOrder;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RepairOrderServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 28);
    private static final Car CAR = new Car(1, 1, "Иванов", "Toyota", "Camry", 2018, "А123ВС77");
    private static final Mechanic MECHANIC = new Mechanic(2, "Белов Игорь", "Ходовая часть");

    private static final class StubOrderDao extends RepairOrderDao {
        boolean numberExists;
        int activeByCar;
        int activeByMechanic;
        Integer lastExcludeId;
        RepairOrder stored;
        boolean deleted;
        OrderData inserted;

        StubOrderDao() {
            super(null);
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
        public int insert(OrderData order) {
            inserted = order;
            return 42;
        }

        @Override
        public boolean update(OrderData order) {
            return stored != null;
        }

        @Override
        public Optional<RepairOrder> findById(int id) {
            return Optional.ofNullable(stored);
        }

        @Override
        public boolean delete(int id) {
            deleted = true;
            return true;
        }

        @Override
        public int findMaxNumberSequence() {
            return 30;
        }
    }

    private static final class StubCarDao extends CarDao {
        boolean exists = true;

        StubCarDao() {
            super(null);
        }

        @Override
        public boolean existsById(int id) {
            return exists;
        }
    }

    private static final class StubMechanicDao extends MechanicDao {
        StubMechanicDao() {
            super(null);
        }

        @Override
        public boolean existsById(int id) {
            return true;
        }
    }

    private final StubOrderDao orderDao = new StubOrderDao();
    private final StubCarDao carDao = new StubCarDao();
    private final RepairOrderService service = new RepairOrderService(orderDao, carDao, new StubMechanicDao(),
            new OrderValidator(Clock.fixed(TODAY.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC)));

    @Test
    void insertsValidNewOrder() {
        assertThat(service.save(input(null, OrderStatus.NEW))).isEqualTo(42);
        assertThat(orderDao.inserted.orderNumber()).isEqualTo("ЗН-0031");
    }

    @Test
    void suggestsNextNumber() {
        assertThat(service.suggestNextNumber()).isEqualTo("ЗН-0031");
    }

    @Test
    void rejectsDuplicateNumberAndPassesOwnIdWhenEditing() {
        orderDao.numberExists = true;
        assertFieldError(input(7, OrderStatus.NEW), OrderValidator.NUMBER);
        assertThat(orderDao.lastExcludeId).isEqualTo(7);
    }

    @Test
    void rejectsSecondActiveOrderForCar() {
        orderDao.activeByCar = 1;
        assertFieldError(input(null, OrderStatus.IN_PROGRESS), OrderValidator.CAR);
    }

    @Test
    void closedOrderIgnoresActiveLimits() {
        orderDao.activeByCar = 1;
        orderDao.activeByMechanic = RepairOrderService.MAX_ACTIVE_ORDERS_PER_MECHANIC;
        OrderInput cancelled = new OrderInput(null, "ЗН-0031", CAR, MECHANIC, "Замена масла", OrderStatus.CANCELLED,
                TODAY, TODAY, "0");
        assertThat(service.save(cancelled)).isEqualTo(42);
    }

    @Test
    void rejectsOverloadedMechanic() {
        orderDao.activeByMechanic = RepairOrderService.MAX_ACTIVE_ORDERS_PER_MECHANIC;
        assertFieldError(input(null, OrderStatus.NEW), OrderValidator.MECHANIC);
    }

    @Test
    void rejectsMissingCar() {
        carDao.exists = false;
        assertFieldError(input(null, OrderStatus.NEW), OrderValidator.CAR);
    }

    @Test
    void updateOfDeletedOrderFails() {
        assertThatThrownBy(() -> service.save(input(7, OrderStatus.NEW)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void doneOrderCannotBeDeleted() {
        orderDao.stored = TestOrders.order(5, OrderStatus.DONE, "100", TODAY, TODAY, 2, "Белов");
        assertThatThrownBy(() -> service.delete(orderDao.stored)).isInstanceOf(BusinessRuleException.class);
        assertThat(orderDao.deleted).isFalse();
    }

    @Test
    void deletesNotCompletedOrderAndReportsAlreadyDeleted() {
        RepairOrder order = TestOrders.order(5, OrderStatus.NEW, "0", TODAY, null, 2, "Белов");
        orderDao.stored = order;
        service.delete(order);
        assertThat(orderDao.deleted).isTrue();

        orderDao.stored = null;
        assertThatThrownBy(() -> service.delete(order)).isInstanceOf(BusinessRuleException.class);
    }

    private void assertFieldError(OrderInput input, String field) {
        assertThatThrownBy(() -> service.save(input))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.errors()).extracting(FieldError::field).contains(field));
    }

    private static OrderInput input(Integer id, OrderStatus status) {
        return new OrderInput(id, "ЗН-0031", CAR, MECHANIC, "Замена масла и фильтров", status, TODAY, null, "0");
    }
}
