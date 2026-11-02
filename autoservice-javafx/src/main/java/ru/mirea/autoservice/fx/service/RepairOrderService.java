package ru.mirea.autoservice.fx.service;

import ru.mirea.autoservice.fx.dao.CarDao;
import ru.mirea.autoservice.fx.dao.MechanicDao;
import ru.mirea.autoservice.fx.dao.RepairOrderDao;
import ru.mirea.autoservice.fx.model.OrderData;
import ru.mirea.autoservice.fx.model.OrderStatus;
import ru.mirea.autoservice.fx.model.RepairOrder;

import java.util.ArrayList;
import java.util.List;

public class RepairOrderService {

    public static final int MAX_ACTIVE_ORDERS_PER_MECHANIC = 5;

    private final RepairOrderDao orderDao;
    private final CarDao carDao;
    private final MechanicDao mechanicDao;
    private final OrderValidator validator;

    public RepairOrderService(RepairOrderDao orderDao, CarDao carDao, MechanicDao mechanicDao,
                              OrderValidator validator) {
        this.orderDao = orderDao;
        this.carDao = carDao;
        this.mechanicDao = mechanicDao;
        this.validator = validator;
    }

    public List<RepairOrder> findAll() {
        return orderDao.findAll();
    }

    public String suggestNextNumber() {
        return String.format("ЗН-%04d", orderDao.findMaxNumberSequence() + 1);
    }

    public int save(OrderInput input) {
        OrderData order = validator.validate(input);
        checkBusinessRules(order);
        if (order.id() == null) {
            return orderDao.insert(order);
        }
        if (!orderDao.update(order)) {
            throw new BusinessRuleException("Заказ-наряд " + order.orderNumber()
                    + " не найден: возможно, его уже удалили. Обновите список.");
        }
        return order.id();
    }

    public void delete(RepairOrder order) {
        RepairOrder current = orderDao.findById(order.id()).orElseThrow(() -> new BusinessRuleException(
                "Заказ-наряд " + order.orderNumber() + " уже удалён. Обновите список."));
        if (current.status() == OrderStatus.DONE) {
            throw new BusinessRuleException("Выполненный заказ-наряд " + current.orderNumber()
                    + " нельзя удалить: он входит в отчётность по выручке. Если заказ создан по ошибке, "
                    + "переведите его в статус «Отменён».");
        }
        if (!orderDao.delete(current.id())) {
            throw new BusinessRuleException("Заказ-наряд " + current.orderNumber() + " уже удалён. Обновите список.");
        }
    }

    private void checkBusinessRules(OrderData order) {
        List<FieldError> errors = new ArrayList<>();
        if (orderDao.existsByNumber(order.orderNumber(), order.id())) {
            errors.add(new FieldError(OrderValidator.NUMBER,
                    "Номер: заказ-наряд " + order.orderNumber() + " уже существует, укажите другой номер."));
        }
        if (!carDao.existsById(order.carId())) {
            errors.add(new FieldError(OrderValidator.CAR, "Автомобиль: выбранный автомобиль не найден в базе, обновите список."));
        }
        if (!mechanicDao.existsById(order.mechanicId())) {
            errors.add(new FieldError(OrderValidator.MECHANIC, "Механик: выбранный механик не найден в базе, обновите список."));
        }
        if (order.status().isActive()) {
            if (orderDao.countActiveByCar(order.carId(), order.id()) > 0) {
                errors.add(new FieldError(OrderValidator.CAR,
                        "Автомобиль: по нему уже есть незакрытый заказ-наряд. Закройте его или дополните существующий."));
            }
            if (orderDao.countActiveByMechanic(order.mechanicId(), order.id()) >= MAX_ACTIVE_ORDERS_PER_MECHANIC) {
                errors.add(new FieldError(OrderValidator.MECHANIC, "Механик: уже ведёт "
                        + MAX_ACTIVE_ORDERS_PER_MECHANIC + " активных заказов, выберите другого механика."));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
