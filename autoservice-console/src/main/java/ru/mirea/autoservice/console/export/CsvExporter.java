package ru.mirea.autoservice.console.export;

import ru.mirea.autoservice.console.model.RepairOrder;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CsvExporter implements DataExporter {

    static final List<String> HEADERS = List.of(
            "Номер", "Дата приёма", "Дата выдачи", "Статус", "Клиент", "Телефон",
            "Автомобиль", "Год", "Госномер", "Механик", "Описание работ", "Стоимость, руб.");

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String SEPARATOR = ";";
    private static final String LINE_END = "\r\n";

    @Override
    public String title() {
        return "CSV (.csv)";
    }

    @Override
    public String extension() {
        return "csv";
    }

    @Override
    public void export(List<RepairOrder> orders, Path file) throws IOException {
        Path absolute = file.toAbsolutePath();
        Path parent = absolute.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (Writer writer = Files.newBufferedWriter(absolute, StandardCharsets.UTF_8)) {
            write(orders, writer);
        }
    }

    void write(List<RepairOrder> orders, Writer writer) throws IOException {
        writer.write('\uFEFF');
        writer.write(row(HEADERS));
        for (RepairOrder order : orders) {
            writer.write(row(List.of(
                    order.orderNumber(),
                    date(order.createdDate()),
                    date(order.completedDate()),
                    order.status().title(),
                    order.clientName(),
                    order.clientPhone(),
                    order.carTitle(),
                    String.valueOf(order.carYear()),
                    order.plate(),
                    order.mechanicName(),
                    order.description(),
                    money(order.cost()))));
        }
    }

    private static String row(List<String> values) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                line.append(SEPARATOR);
            }
            line.append(escape(values.get(i)));
        }
        return line.append(LINE_END).toString();
    }

    static String escape(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        String safe = startsLikeFormula(value) ? "'" + value : value;
        if (safe.contains(SEPARATOR) || safe.contains("\"") || safe.contains("\n") || safe.contains("\r")) {
            return "\"" + safe.replace("\"", "\"\"") + "\"";
        }
        return safe;
    }

    private static boolean startsLikeFormula(String value) {
        char first = value.charAt(0);
        if (first == '=' || first == '@' || first == '\t' || first == '\r') {
            return true;
        }
        return (first == '+' || first == '-') && (value.length() == 1 || !Character.isDigit(value.charAt(1)));
    }

    private static String date(LocalDate date) {
        return date == null ? "" : DATE.format(date);
    }

    private static String money(BigDecimal value) {
        return value == null ? "" : value.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }
}
