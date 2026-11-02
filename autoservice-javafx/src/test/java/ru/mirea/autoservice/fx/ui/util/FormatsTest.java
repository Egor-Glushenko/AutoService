package ru.mirea.autoservice.fx.ui.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FormatsTest {

    @Test
    void parsesStrictDates() {
        assertThat(Formats.parseDate(" 01.10.2026 ")).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(Formats.parseDate("")).isNull();
        assertThat(Formats.parseDate(null)).isNull();
        assertThatThrownBy(() -> Formats.parseDate("31.02.2026")).isInstanceOf(DateTimeParseException.class);
        assertThatThrownBy(() -> Formats.parseDate("abc")).isInstanceOf(DateTimeParseException.class);
    }

    @Test
    void formatsValues() {
        assertThat(Formats.date(LocalDate.of(2026, 1, 5))).isEqualTo("05.01.2026");
        assertThat(Formats.date(null)).isEqualTo("—");
        assertThat(Formats.money(null)).isEqualTo("—");
        assertThat(Formats.money(new BigDecimal("1234.5"))).endsWith(",50 ₽");
        assertThat(Formats.shortName("Королёв Андрей Михайлович")).isEqualTo("Королёв А. М.");
    }
}
