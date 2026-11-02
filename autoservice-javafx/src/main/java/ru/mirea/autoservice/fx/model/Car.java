package ru.mirea.autoservice.fx.model;

public record Car(int id, int clientId, String ownerName, String brand, String model, int year, String plate) {

    public String title() {
        return brand + " " + model;
    }

    @Override
    public String toString() {
        return title() + " · " + plate + " · " + ownerName;
    }
}
