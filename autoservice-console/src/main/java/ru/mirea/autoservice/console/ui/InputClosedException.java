package ru.mirea.autoservice.console.ui;

public class InputClosedException extends RuntimeException {

    public InputClosedException() {
        super("Поток ввода закрыт");
    }
}
