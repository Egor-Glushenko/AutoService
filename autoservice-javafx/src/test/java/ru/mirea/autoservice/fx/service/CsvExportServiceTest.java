package ru.mirea.autoservice.fx.service;

import org.junit.jupiter.api.Test;
import ru.mirea.autoservice.fx.model.OrderStatus;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvExportServiceTest {

    private final CsvExportService service = new CsvExportService();

    @Test
    void writesHeaderAndRowsForExcel() throws IOException {
        StringWriter out = new StringWriter();
        service.write(List.of(TestOrders.order(12, OrderStatus.DONE, "19600.5",
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 2), 2, "Белов Игорь")), out);

        String[] lines = out.toString().split("\r\n");
        assertThat(lines[0]).startsWith("\uFEFFНомер;Дата приёма;");
        assertThat(lines[1]).isEqualTo("ЗН-0012;01.08.2026;02.08.2026;Выполнен;Иванов Иван Петрович;+79161234501;"
                + "Toyota Camry;2018;А123ВС77;Белов Игорь;Замена тормозных колодок;19600,50");
    }

    @Test
    void escapesSpecialCharactersAndFormulas() {
        assertThat(CsvExportService.escape("a;b")).isEqualTo("\"a;b\"");
        assertThat(CsvExportService.escape("say \"hi\"")).isEqualTo("\"say \"\"hi\"\"\"");
        assertThat(CsvExportService.escape("line1\nline2")).isEqualTo("\"line1\nline2\"");
        assertThat(CsvExportService.escape("=SUM(A1)")).isEqualTo("'=SUM(A1)");
        assertThat(CsvExportService.escape("+79161234501")).isEqualTo("+79161234501");
        assertThat(CsvExportService.escape(null)).isEmpty();
    }
}
