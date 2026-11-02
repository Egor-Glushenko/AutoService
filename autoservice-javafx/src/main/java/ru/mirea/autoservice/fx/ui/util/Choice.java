package ru.mirea.autoservice.fx.ui.util;

public record Choice<T>(T value, String label) {

    @Override
    public String toString() {
        return label;
    }
}
