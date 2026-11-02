package ru.mirea.autoservice.console.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;

public final class Formats {

    public static final Locale RU = Locale.forLanguageTag("ru-RU");
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
        if (fullName == null || fullName.isBlank()) {
            return "—";
        }
        String[] parts = fullName.strip().split("\\s+");
        StringBuilder result = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                result.append(' ').append(parts[i].charAt(0)).append('.');
            }
        }
        return result.toString();
    }

    public static String text(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
}
