package ru.mirea.autoservice.console.export;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.mirea.autoservice.console.model.OrderStatus;
import ru.mirea.autoservice.console.model.RepairOrder;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelExporterTest {

    private final ExcelExporter exporter = new ExcelExporter();

    @Test
    void createsWorkbookWithHeaderAndData(@TempDir Path tempDir) throws IOException {
        RepairOrder order = new RepairOrder(12, "ЗН-0012", 1, "Toyota", "Camry", 2018, "А123ВС77",
                "Иванов Иван Петрович", "+79161234501", 2, "Белов Игорь", "Ходовая часть",
                "Замена тормозных колодок", OrderStatus.DONE, LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 2), new BigDecimal("19600.5"));

        Path file = tempDir.resolve("orders.xlsx");
        exporter.export(List.of(order), file);

        assertThat(file).exists();
        try (InputStream in = Files.newInputStream(file);
             XSSFWorkbook workbook = new XSSFWorkbook(in)) {
            Sheet sheet = workbook.getSheet("Заказ-наряды");
            assertThat(sheet).isNotNull();
            assertThat(sheet.getLastRowNum()).isEqualTo(1);

            Row header = sheet.getRow(0);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("Номер");
            assertThat(header.getCell(11).getStringCellValue()).isEqualTo("Стоимость, руб.");

            Row row = sheet.getRow(1);
            assertThat(row.getCell(0).getStringCellValue()).isEqualTo("ЗН-0012");
            assertThat(row.getCell(3).getStringCellValue()).isEqualTo("Выполнен");
            assertThat(row.getCell(1).getLocalDateTimeCellValue().toLocalDate())
                    .isEqualTo(LocalDate.of(2026, 8, 1));
            assertThat(row.getCell(11).getNumericCellValue()).isEqualTo(19600.5);
        }
    }
}
