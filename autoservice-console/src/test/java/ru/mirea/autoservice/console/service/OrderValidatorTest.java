package ru.mirea.autoservice.console.service;

import org.junit.jupiter.api.Test;
import ru.mirea.autoservice.console.exception.FieldError;
import ru.mirea.autoservice.console.exception.ValidationException;
import ru.mirea.autoservice.console.model.OrderData;
import ru.mirea.autoservice.console.model.OrderInput;
import ru.mirea.autoservice.console.model.OrderStatus;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderValidatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 29);

    private final OrderValidator validator = new OrderValidator(
            Clock.fixed(TODAY.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC));

    @Test
    void acceptsValidOrderAndNormalizesValues() {
        OrderData data = validator.validate(input(" ЗН-0001 ", 1, 2, "  Замена масла  ",
                OrderStatus.NEW, TODAY, null, "12500,50"));

        assertThat(data.orderNumber()).isEqualTo("ЗН-0001");
        assertThat(data.description()).isEqualTo("Замена масла");
        assertThat(data.cost()).isEqualByComparingTo("12500.50");
        assertThat(data.status()).isEqualTo(OrderStatus.NEW);
        assertThat(data.completedDate()).isNull();
    }

    @Test
    void rejectsWrongNumberFormat() {
        assertFieldError(input("0001", 1, 1, "Замена масла", OrderStatus.NEW, TODAY, null, "0"),
                OrderValidator.NUMBER);
        assertFieldError(input("ЗН-1", 1, 1, "Замена масла", OrderStatus.NEW, TODAY, null, "0"),
                OrderValidator.NUMBER);
    }

    @Test
    void rejectsDoneOrderWithoutCompletedDate() {
        assertFieldError(input("ЗН-0001", 1, 1, "Замена масла", OrderStatus.DONE, TODAY, null, "1000"),
                OrderValidator.COMPLETED);
    }

    @Test
    void rejectsCompletedDateForActiveOrder() {
        assertFieldError(input("ЗН-0001", 1, 1, "Замена масла", OrderStatus.IN_PROGRESS, TODAY, TODAY, "1000"),
                OrderValidator.COMPLETED);
    }

    @Test
    void rejectsFutureCreatedDate() {
        assertFieldError(input("ЗН-0001", 1, 1, "Замена масла", OrderStatus.NEW, TODAY.plusDays(1), null, "0"),
                OrderValidator.CREATED);
    }

    @Test
    void rejectsCompletedDateBeforeCreatedDate() {
        assertFieldError(input("ЗН-0001", 1, 1, "Замена масла", OrderStatus.DONE, TODAY, TODAY.minusDays(1), "1000"),
                OrderValidator.COMPLETED);
    }

    @Test
    void rejectsTooLargeAndNegativeCost() {
        assertFieldError(input("ЗН-0001", 1, 1, "Замена масла", OrderStatus.NEW, TODAY, null, "10000001"),
                OrderValidator.COST);
        assertFieldError(input("ЗН-0001", 1, 1, "Замена масла", OrderStatus.NEW, TODAY, null, "-1"),
                OrderValidator.COST);
    }

    @Test
    void rejectsZeroCostForDoneOrder() {
        assertFieldError(input("ЗН-0001", 1, 1, "Замена масла", OrderStatus.DONE, TODAY, TODAY, "0"),
                OrderValidator.COST);
    }

    @Test
    void rejectsTooManyDecimalPlaces() {
        assertFieldError(input("ЗН-0001", 1, 1, "Замена масла", OrderStatus.NEW, TODAY, null, "10,999"),
                OrderValidator.COST);
    }

    @Test
    void rejectsShortDescriptionAndMissingReferences() {
        assertFieldError(input("ЗН-0001", 1, 1, "ТО", OrderStatus.NEW, TODAY, null, "0"),
                OrderValidator.DESCRIPTION);
        assertFieldError(input("ЗН-0001", null, null, "Замена масла", OrderStatus.NEW, TODAY, null, "0"),
                OrderValidator.CAR);
    }

    private void assertFieldError(OrderInput input, String field) {
        assertThatThrownBy(() -> validator.validate(input))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.errors()).extracting(FieldError::field).contains(field));
    }

    private static OrderInput input(String number, Integer carId, Integer mechanicId, String description,
                                    OrderStatus status, LocalDate created, LocalDate completed, String cost) {
        return new OrderInput(null, number, carId, mechanicId, description, status, created, completed, cost);
    }
}
