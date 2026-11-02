package ru.mirea.autoservice.console.export;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.mirea.autoservice.console.model.RepairOrder;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class ExcelExporter implements DataExporter {

    private static final List<String> HEADERS = List.of(
            "Номер", "Дата приёма", "Дата выдачи", "Статус", "Клиент", "Телефон",
            "Автомобиль", "Год", "Госномер", "Механик", "Описание работ", "Стоимость, руб.");

    @Override
    public String title() {
        return "Excel (.xlsx)";
    }

    @Override
    public String extension() {
        return "xlsx";
    }

    @Override
    public void export(List<RepairOrder> orders, Path file) throws IOException {
        Path absolute = file.toAbsolutePath();
        Path parent = absolute.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Заказ-наряды");
            CellStyle headerStyle = headerStyle(workbook);
            CellStyle dateStyle = dateStyle(workbook);
            CellStyle moneyStyle = moneyStyle(workbook);

            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.size(); i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(HEADERS.get(i));
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (RepairOrder order : orders) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(order.orderNumber());
                setDate(row.createCell(1), order.createdDate(), dateStyle);
                setDate(row.createCell(2), order.completedDate(), dateStyle);
                row.createCell(3).setCellValue(order.status().title());
                row.createCell(4).setCellValue(order.clientName());
                row.createCell(5).setCellValue(order.clientPhone());
                row.createCell(6).setCellValue(order.carTitle());
                row.createCell(7).setCellValue(order.carYear());
                row.createCell(8).setCellValue(order.plate());
                row.createCell(9).setCellValue(order.mechanicName());
                row.createCell(10).setCellValue(order.description());
                Cell cost = row.createCell(11);
                BigDecimal value = order.cost();
                cost.setCellValue(value == null ? 0 : value.doubleValue());
                cost.setCellStyle(moneyStyle);
            }

            int[] widths = {12, 12, 12, 11, 28, 15, 20, 7, 12, 28, 60, 15};
            for (int i = 0; i < widths.length; i++) {
                sheet.setColumnWidth(i, widths[i] * 256);
            }
            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new CellRangeAddress(0, Math.max(0, orders.size()), 0, HEADERS.size() - 1));

            try (OutputStream out = Files.newOutputStream(absolute)) {
                workbook.write(out);
            }
        }
    }

    private static void setDate(Cell cell, LocalDate date, CellStyle style) {
        if (date == null) {
            cell.setBlank();
            return;
        }
        cell.setCellValue(date);
        cell.setCellStyle(style);
    }

    private static CellStyle headerStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private static CellStyle dateStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("dd.mm.yyyy"));
        return style;
    }

    private static CellStyle moneyStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }
}
