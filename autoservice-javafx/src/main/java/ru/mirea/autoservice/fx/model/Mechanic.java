package ru.mirea.autoservice.fx.model;

public record Mechanic(int id, String fullName, String specialization) {

    @Override
    public String toString() {
        return fullName + " (" + specialization + ")";
    }
}
