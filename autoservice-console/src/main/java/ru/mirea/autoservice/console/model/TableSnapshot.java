package ru.mirea.autoservice.console.model;

import java.util.List;

public record TableSnapshot(String title, List<String> headers, List<List<String>> rows) {
}
