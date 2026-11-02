package ru.mirea.autoservice.console.service;

import ru.mirea.autoservice.console.exception.BusinessException;
import ru.mirea.autoservice.console.exception.EntityNotFoundException;
import ru.mirea.autoservice.console.exception.FieldError;
import ru.mirea.autoservice.console.exception.ValidationException;
import ru.mirea.autoservice.console.model.OrderData;
import ru.mirea.autoservice.console.model.OrderInput;
import ru.mirea.autoservice.console.model.OrderStatus;
import ru.mirea.autoservice.console.model.RepairOrder;
import ru.mirea.autoservice.console.repository.CarRepository;
import ru.mirea.autoservice.console.repository.MechanicRepository;
import ru.mirea.autoservice.console.repository.RepairOrderRepository;

import java.util.ArrayList;
import java.util.List;

public class RepairOrderService {

    public static final int MAX_ACTIVE_ORDERS_PER_MECHANIC = 5;

    private final RepairOrderRepository orders;
    private final CarRepository cars;
    private final MechanicRepository mechanics;
    private final OrderValidator validator;

    public RepairOrderService(RepairOrderRepository orders, CarRepository cars, MechanicRepository mechanics,
                              OrderValidator validator) {
        this.orders = orders;
        this.cars = cars;
        this.mechanics = mechanics;
        this.validator = validator;
    }

    public List<RepairOrder> findAll() {
        return orders.findAll();
    }

    public RepairOrder getRequired(int id) {
        return orders.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Заказ-наряд с ID " + id + " не найден."));
    }

    public String suggestNextNumber() {
        return String.format("ЗН-%04d", orders.maxNumberSequence() + 1);
    }

    public int create(OrderInput input) {
        OrderData data = validator.validate(new OrderInput(null, input.orderNumber(), input.carId(),
                input.mechanicId(), input.description(), input.status(), input.createdDate(),
                input.completedDate(), input.costText()));
        checkBusinessRules(data);
        return orders.insert(data);
    }

    public void update(int id, OrderInput input) {
        getRequired(id);
        OrderData data = validator.validate(new OrderInput(id, input.orderNumber(), input.carId(),
                input.mechanicId(), input.description(), input.status(), input.createdDate(),
                input.completedDate(), input.costText()));
        checkBusinessRules(data);
        if (!orders.update(data)) {
            throw new EntityNotFoundException("Заказ-наряд с ID " + id + " не найден: возможно, его уже удалили.");
        }
    }

    public void delete(int id) {
        RepairOrder order = getRequired(id);
        if (order.status() == OrderStatus.DONE) {
            throw new BusinessException("Выполненный заказ-наряд " + order.orderNumber()
                    + " нельзя удалить: он входит в отчётность по выручке. "
                    + "Если заказ создан по ошибке, переведите его в статус «Отменён».");
        }
        if (!orders.delete(id)) {
            throw new EntityNotFoundException("Заказ-наряд " + order.orderNumber()
                    + " уже удалён: возможно, его удалили в другом окне.");
        }
    }

    private void checkBusinessRules(OrderData order) {
        List<FieldError> errors = new ArrayList<>();

        if (orders.existsByNumber(order.orderNumber(), order.id())) {
            errors.add(new FieldError(OrderValidator.NUMBER,
                    "Номер: заказ-наряд " + order.orderNumber() + " уже существует, укажите другой номер."));
        }
        if (!cars.existsById(order.carId())) {
            errors.add(new FieldError(OrderValidator.CAR,
                    "Автомобиль: выбранный автомобиль не найден в базе, обновите список."));
        }
        if (!mechanics.existsById(order.mechanicId())) {
            errors.add(new FieldError(OrderValidator.MECHANIC,
                    "Механик: выбранный механик не найден в базе, обновите список."));
        }

        if (order.status().isActive()) {
            if (orders.countActiveByCar(order.carId(), order.id()) > 0) {
                errors.add(new FieldError(OrderValidator.CAR,
                        "Автомобиль: по нему уже есть незакрытый заказ-наряд. "
                                + "Закройте его или отредактируйте существующий."));
            }
            if (orders.countActiveByMechanic(order.mechanicId(), order.id())
                    >= MAX_ACTIVE_ORDERS_PER_MECHANIC) {
                errors.add(new FieldError(OrderValidator.MECHANIC, "Механик: уже ведёт "
                        + MAX_ACTIVE_ORDERS_PER_MECHANIC + " активных заказов, выберите другого механика."));
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
