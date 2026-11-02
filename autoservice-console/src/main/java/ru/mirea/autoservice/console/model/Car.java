package ru.mirea.autoservice.console.model;

public record Car(int id, int clientId, String ownerName, String brand, String model, int year, String plate) {

    public String title() {
        return brand + " " + model;
    }
}
