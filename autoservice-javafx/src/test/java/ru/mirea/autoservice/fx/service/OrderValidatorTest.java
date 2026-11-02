package ru.mirea.autoservice.fx.service;

import org.junit.jupiter.api.Test;
import ru.mirea.autoservice.fx.model.Car;
import ru.mirea.autoservice.fx.model.Mechanic;
import ru.mirea.autoservice.fx.model.OrderData;
import ru.mirea.autoservice.fx.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderValidatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 28);
    private static final Car CAR = new Car(3, 1, "Иванов И. П.", "Kia", "Rio", 2019, "Е789КМ77");
    private static final Mechanic MECHANIC = new Mechanic(2, "Белов Игорь", "Ходовая часть");

    private final OrderValidator validator = new OrderValidator(
            Clock.fixed(TODAY.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC));

    @Test
    void acceptsValidOrderAndNormalizesValues() {
        OrderData data = validator.validate(input(" ЗН-0031 ", OrderStatus.DONE,
                TODAY.minusDays(2), TODAY, "12 500,5"));

        assertThat(data.orderNumber()).isEqualTo("ЗН-0031");
        assertThat(data.cost()).isEqualByComparingTo("12500.50");
        assertThat(data.carId()).isEqualTo(3);
        assertThat(data.mechanicId()).isEqualTo(2);
    }

    @Test
    void reportsAllEmptyRequiredFieldsAtOnce() {
        OrderInput empty = new OrderInput(null, "", null, null, "  ", null, null, null, "");

        assertThatThrownBy(() -> validator.validate(empty))
                .isInstanceOfSatisfying(ValidationException.class, e -> assertThat(e.errors())
                        .extracting(FieldError::field)
                        .contains(OrderValidator.NUMBER, OrderValidator.CAR, OrderValidator.MECHANIC,
                                OrderValidator.DESCRIPTION, OrderValidator.STATUS, OrderValidator.CREATED,
                                OrderValidator.COST));
    }

    @Test
    void rejectsWrongNumberFormat() {
        assertFieldError(input("ZN-1", OrderStatus.NEW, TODAY, null, "0"), OrderValidator.NUMBER);
    }

    @Test
    void rejectsNonNumericNegativeAndTooPreciseCost() {
        assertFieldError(input("ЗН-0001", OrderStatus.NEW, TODAY, null, "abc"), OrderValidator.COST);
        assertFieldError(input("ЗН-0001", OrderStatus.NEW, TODAY, null, "-5"), OrderValidator.COST);
        assertFieldError(input("ЗН-0001", OrderStatus.NEW, TODAY, null, "10.555"), OrderValidator.COST);
        assertFieldError(input("ЗН-0001", OrderStatus.NEW, TODAY, null, "10000000.01"), OrderValidator.COST);
    }

    @Test
    void doneOrderNeedsCompletedDateAndPositiveCost() {
        assertFieldError(input("ЗН-0001", OrderStatus.DONE, TODAY, null, "100"), OrderValidator.COMPLETED);
        assertFieldError(input("ЗН-0001", OrderStatus.DONE, TODAY, TODAY, "0"), OrderValidator.COST);
    }

    @Test
    void activeOrderMustNotHaveCompletedDate() {
        assertFieldError(input("ЗН-0001", OrderStatus.IN_PROGRESS, TODAY.minusDays(1), TODAY, "0"),
                OrderValidator.COMPLETED);
    }

    @Test
    void checksDateLogic() {
        assertFieldError(input("ЗН-0001", OrderStatus.NEW, TODAY.plusDays(1), null, "0"), OrderValidator.CREATED);
        assertFieldError(input("ЗН-0001", OrderStatus.CANCELLED, TODAY, TODAY.minusDays(1), "0"),
                OrderValidator.COMPLETED);
        assertFieldError(input("ЗН-0001", OrderStatus.NEW, LocalDate.of(1999, 12, 31), null, "0"),
                OrderValidator.CREATED);
    }

    @Test
    void checksDescriptionLength() {
        OrderInput tooShort = new OrderInput(null, "ЗН-0001", CAR, MECHANIC, "ТО", OrderStatus.NEW, TODAY, null, "0");
        OrderInput tooLong = new OrderInput(null, "ЗН-0001", CAR, MECHANIC, "x".repeat(501), OrderStatus.NEW,
                TODAY, null, "0");
        assertFieldError(tooShort, OrderValidator.DESCRIPTION);
        assertFieldError(tooLong, OrderValidator.DESCRIPTION);
    }

    @Test
    void zeroCostAllowedForNewOrder() {
        assertThat(validator.validate(input("ЗН-0001", OrderStatus.NEW, TODAY, null, "0")).cost())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    private void assertFieldError(OrderInput input, String field) {
        assertThatThrownBy(() -> validator.validate(input))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.errors()).extracting(FieldError::field).contains(field));
    }

    private static OrderInput input(String number, OrderStatus status, LocalDate created, LocalDate completed,
                                    String cost) {
        return new OrderInput(null, number, CAR, MECHANIC, "Замена передних колодок", status, created, completed, cost);
    }
}
