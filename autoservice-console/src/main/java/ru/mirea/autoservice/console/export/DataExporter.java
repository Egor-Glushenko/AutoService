package ru.mirea.autoservice.console.export;

import ru.mirea.autoservice.console.model.RepairOrder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface DataExporter {

    String title();

    String extension();

    void export(List<RepairOrder> orders, Path file) throws IOException;
}
