package ru.mirea.autoservice.fx.model;

public enum OrderStatus {
    NEW("Новый"),
    IN_PROGRESS("В работе"),
    DONE("Выполнен"),
    CANCELLED("Отменён");

    private final String title;

    OrderStatus(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }

    public boolean isActive() {
        return this == NEW || this == IN_PROGRESS;
    }

    @Override
    public String toString() {
        return title;
    }
}
