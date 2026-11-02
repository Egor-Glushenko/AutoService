package ru.mirea.autoservice.fx.ui.controller;

public enum OrderSortOption {
    CREATED_DESC("Дата приёма: сначала новые", Field.CREATED, false),
    CREATED_ASC("Дата приёма: сначала старые", Field.CREATED, true),
    COST_DESC("Стоимость: по убыванию", Field.COST, false),
    COST_ASC("Стоимость: по возрастанию", Field.COST, true),
    CLIENT_ASC("Клиент: от А до Я", Field.CLIENT, true),
    NUMBER_ASC("Номер: по возрастанию", Field.NUMBER, true);

    enum Field { CREATED, COST, CLIENT, NUMBER }

    private final String label;
    private final Field field;
    private final boolean ascending;

    OrderSortOption(String label, Field field, boolean ascending) {
        this.label = label;
        this.field = field;
        this.ascending = ascending;
    }

    Field field() {
        return field;
    }

    boolean ascending() {
        return ascending;
    }

    @Override
    public String toString() {
        return label;
    }
}
