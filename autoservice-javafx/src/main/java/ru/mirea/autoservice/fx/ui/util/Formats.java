package ru.mirea.autoservice.fx.ui.util;

import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

public final class Formats {

    public static final Locale RU = Locale.forLanguageTag("ru-RU");
    private static final String INVALID_DATE_KEY = "autoservice.invalidDateText";
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT);

    private Formats() {
    }

    public static String date(LocalDate date) {
        return date == null ? "—" : DATE.format(date);
    }

    public static LocalDate parseDate(String text) {
        return text == null || text.isBlank() ? null : LocalDate.parse(text.strip(), DATE);
    }

    public static String money(BigDecimal value) {
        return value == null ? "—" : String.format(RU, "%,.2f ₽", value);
    }

    public static String shortName(String fullName) {
        String[] parts = fullName.strip().split("\\s+");
        StringBuilder result = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            result.append(" ").append(parts[i].charAt(0)).append('.');
        }
        return result.toString();
    }

    public static void installDateConverter(DatePicker picker) {
        picker.setConverter(dateConverter(picker));
        picker.valueProperty().addListener((obs, old, value) -> clearInvalidDate(picker));
    }

    private static StringConverter<LocalDate> dateConverter(DatePicker picker) {
        return new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : DATE.format(date);
            }

            @Override
            public LocalDate fromString(String text) {
                try {
                    LocalDate date = parseDate(text);
                    picker.getProperties().remove(INVALID_DATE_KEY);
                    return date;
                } catch (DateTimeParseException e) {
                    picker.getProperties().put(INVALID_DATE_KEY, text.strip());
                    return picker.getValue();
                }
            }
        };
    }

    public static String invalidDateText(DatePicker picker) {
        return (String) picker.getProperties().get(INVALID_DATE_KEY);
    }

    public static void clearInvalidDate(DatePicker picker) {
        picker.getProperties().remove(INVALID_DATE_KEY);
    }
}
