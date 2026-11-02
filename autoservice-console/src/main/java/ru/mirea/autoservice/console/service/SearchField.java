package ru.mirea.autoservice.console.service;

import ru.mirea.autoservice.console.model.RepairOrder;

import java.util.function.Function;

public enum SearchField {

    NUMBER("номер заказа", RepairOrder::orderNumber),
    CLIENT("клиент (ФИО или телефон)", order -> order.clientName() + " " + order.clientPhone()),
    CAR("автомобиль (марка, модель, госномер)", order -> order.carTitle() + " " + order.plate()),
    DESCRIPTION("описание работ", RepairOrder::description),
    MECHANIC("механик", RepairOrder::mechanicName);

    private final String title;
    private final Function<RepairOrder, String> extractor;

    SearchField(String title, Function<RepairOrder, String> extractor) {
        this.title = title;
        this.extractor = extractor;
    }

    public String title() {
        return title;
    }

    public String text(RepairOrder order) {
        return extractor.apply(order);
    }
}
