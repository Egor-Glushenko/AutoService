package ru.mirea.autoservice.fx.service;

import ru.mirea.autoservice.fx.model.OrderData;
import ru.mirea.autoservice.fx.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class OrderValidator {

    public static final String NUMBER = "orderNumber";
    public static final String CAR = "car";
    public static final String MECHANIC = "mechanic";
    public static final String DESCRIPTION = "description";
    public static final String STATUS = "status";
    public static final String CREATED = "createdDate";
    public static final String COMPLETED = "completedDate";
    public static final String COST = "cost";

    public static final int DESCRIPTION_MIN = 5;
    public static final int DESCRIPTION_MAX = 500;
    public static final BigDecimal COST_MAX = new BigDecimal("10000000");
    static final LocalDate EARLIEST_DATE = LocalDate.of(2000, 1, 1);
    private static final Pattern NUMBER_PATTERN = Pattern.compile("ЗН-\\d{4,6}");

    private final Clock clock;

    public OrderValidator(Clock clock) {
        this.clock = clock;
    }

    public OrderData validate(OrderInput input) {
        List<FieldError> errors = new ArrayList<>();
        LocalDate today = LocalDate.now(clock);

        String number = trim(input.orderNumber());
        if (number.isEmpty()) {
            errors.add(new FieldError(NUMBER, "Номер: заполните поле (формат ЗН-0001)."));
        } else if (!NUMBER_PATTERN.matcher(number).matches()) {
            errors.add(new FieldError(NUMBER, "Номер: используйте формат ЗН-0001 (буквы ЗН, дефис и 4–6 цифр)."));
        }

        if (input.car() == null) {
            errors.add(new FieldError(CAR, "Автомобиль: выберите автомобиль из списка."));
        }
        if (input.mechanic() == null) {
            errors.add(new FieldError(MECHANIC, "Механик: выберите ответственного механика."));
        }

        String description = trim(input.description());
        if (description.length() < DESCRIPTION_MIN || description.length() > DESCRIPTION_MAX) {
            errors.add(new FieldError(DESCRIPTION, "Описание работ: от " + DESCRIPTION_MIN + " до "
                    + DESCRIPTION_MAX + " символов (сейчас " + description.length() + ")."));
        }

        OrderStatus status = input.status();
        if (status == null) {
            errors.add(new FieldError(STATUS, "Статус: выберите статус заказа."));
        }

        LocalDate created = input.createdDate();
        if (created == null) {
            errors.add(new FieldError(CREATED, "Дата приёма: укажите дату."));
        } else if (created.isAfter(today)) {
            errors.add(new FieldError(CREATED, "Дата приёма: не может быть позже сегодняшнего дня."));
        } else if (created.isBefore(EARLIEST_DATE)) {
            errors.add(new FieldError(CREATED, "Дата приёма: не может быть раньше 01.01.2000."));
        }

        LocalDate completed = input.completedDate();
        if (status == OrderStatus.DONE && completed == null) {
            errors.add(new FieldError(COMPLETED, "Дата выдачи: обязательна для выполненного заказа."));
        } else if (status != null && status.isActive() && completed != null) {
            errors.add(new FieldError(COMPLETED, "Дата выдачи: заполняется только для выполненного или отменённого заказа."));
        } else if (completed != null && completed.isAfter(today)) {
            errors.add(new FieldError(COMPLETED, "Дата выдачи: не может быть позже сегодняшнего дня."));
        } else if (completed != null && created != null && completed.isBefore(created)) {
            errors.add(new FieldError(COMPLETED, "Дата выдачи: не может быть раньше даты приёма."));
        }

        BigDecimal cost = parseCost(input.costText(), status, errors);

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        return new OrderData(input.id(), number, input.car().id(), input.mechanic().id(),
                description, status, created, completed, cost);
    }

    private static BigDecimal parseCost(String text, OrderStatus status, List<FieldError> errors) {
        String normalized = trim(text).replace(" ", "").replace(" ", "").replace(',', '.');
        if (normalized.isEmpty()) {
            errors.add(new FieldError(COST, "Стоимость: заполните поле (0, если стоимость ещё не известна)."));
            return null;
        }
        BigDecimal cost;
        try {
            cost = new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            errors.add(new FieldError(COST, "Стоимость: введите число, например 12500 или 12500,50."));
            return null;
        }
        if (cost.signum() < 0 || cost.compareTo(COST_MAX) > 0) {
            errors.add(new FieldError(COST, "Стоимость: допустимо от 0 до 10 000 000 ₽."));
        } else if (cost.stripTrailingZeros().scale() > 2) {
            errors.add(new FieldError(COST, "Стоимость: не более двух знаков после запятой."));
        } else if (status == OrderStatus.DONE && cost.signum() == 0) {
            errors.add(new FieldError(COST, "Стоимость: у выполненного заказа должна быть больше 0."));
        }
        return cost;
    }

    private static String trim(String value) {
        return value == null ? "" : value.strip();
    }
}
