package ru.mirea.autoservice.console.service;

import ru.mirea.autoservice.console.model.RepairOrder;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public enum OrderSortOption {

    CREATED_DESC("Дата приёма: сначала новые", Comparator.comparing(RepairOrder::createdDate).reversed()),
    CREATED_ASC("Дата приёма: сначала старые", Comparator.comparing(RepairOrder::createdDate)),
    COST_DESC("Стоимость: по убыванию", Comparator.comparing(RepairOrder::cost).reversed()),
    COST_ASC("Стоимость: по возрастанию", Comparator.comparing(RepairOrder::cost)),
    CLIENT_ASC("Клиент: от А до Я", Comparator.comparing(RepairOrder::clientName, OrderSortOption::compareRussian)),
    NUMBER_ASC("Номер: по возрастанию", Comparator.comparing(RepairOrder::orderNumber));

    private static final Collator COLLATOR = Collator.getInstance(Locale.forLanguageTag("ru-RU"));

    private final String title;
    private final Comparator<RepairOrder> comparator;

    OrderSortOption(String title, Comparator<RepairOrder> comparator) {
        this.title = title;
        this.comparator = comparator;
    }

    public String title() {
        return title;
    }

    public Comparator<RepairOrder> comparator() {
        return comparator;
    }

    private static int compareRussian(String left, String right) {
        return COLLATOR.compare(left, right);
    }
}
