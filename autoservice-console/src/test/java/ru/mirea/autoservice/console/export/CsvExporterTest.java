package ru.mirea.autoservice.console.export;

import org.junit.jupiter.api.Test;
import ru.mirea.autoservice.console.model.OrderStatus;
import ru.mirea.autoservice.console.model.RepairOrder;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvExporterTest {

    private final CsvExporter exporter = new CsvExporter();

    @Test
    void writesHeaderAndRowsForExcel() throws IOException {
        RepairOrder order = new RepairOrder(12, "ЗН-0012", 1, "Toyota", "Camry", 2018, "А123ВС77",
                "Иванов Иван Петрович", "+79161234501", 2, "Белов Игорь", "Ходовая часть",
                "Замена тормозных колодок", OrderStatus.DONE, LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 2), new BigDecimal("19600.5"));

        StringWriter out = new StringWriter();
        exporter.write(List.of(order), out);

        String[] lines = out.toString().split("\r\n");
        assertThat(lines[0]).startsWith("\uFEFFНомер;Дата приёма;");
        assertThat(lines[1]).isEqualTo("ЗН-0012;01.08.2026;02.08.2026;Выполнен;Иванов Иван Петрович;"
                + "+79161234501;Toyota Camry;2018;А123ВС77;Белов Игорь;Замена тормозных колодок;19600,50");
    }

    @Test
    void escapesSpecialCharactersAndFormulas() {
        assertThat(CsvExporter.escape("a;b")).isEqualTo("\"a;b\"");
        assertThat(CsvExporter.escape("say \"hi\"")).isEqualTo("\"say \"\"hi\"\"\"");
        assertThat(CsvExporter.escape("line1\nline2")).isEqualTo("\"line1\nline2\"");
        assertThat(CsvExporter.escape("=SUM(A1)")).isEqualTo("'=SUM(A1)");
        assertThat(CsvExporter.escape("+79161234501")).isEqualTo("+79161234501");
        assertThat(CsvExporter.escape(null)).isEmpty();
        assertThat(CsvExporter.escape("")).isEmpty();
    }
}
