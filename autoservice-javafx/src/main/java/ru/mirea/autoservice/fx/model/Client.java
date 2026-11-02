package ru.mirea.autoservice.fx.model;

public record Client(int id, String fullName, String phone, String email, int carCount, int orderCount) {
}
