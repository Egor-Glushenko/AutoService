package ru.mirea.autoservice.fx.ui.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import ru.mirea.autoservice.fx.AppContext;
import ru.mirea.autoservice.fx.model.OrderStatus;
import ru.mirea.autoservice.fx.model.RepairOrder;
import ru.mirea.autoservice.fx.service.OrderStatistics;
import ru.mirea.autoservice.fx.service.StatisticsService;
import ru.mirea.autoservice.fx.ui.util.Formats;

import java.util.List;
import java.util.Map;

public class StatisticsController {

    @FXML private Label subtitleLabel;
    @FXML private Label totalValue;
    @FXML private Label activeValue;
    @FXML private Label completionValue;
    @FXML private Label revenueValue;
    @FXML private Label averageCheckValue;
    @FXML private Label minMaxValue;
    @FXML private Label durationValue;
    @FXML private Label topMechanicValue;
    @FXML private PieChart statusChart;
    @FXML private BarChart<String, Number> mechanicChart;

    private final StatisticsService statisticsService;

    public StatisticsController(AppContext context) {
        this.statisticsService = context.statistics();
    }

    public void show(List<RepairOrder> orders, int totalInDatabase) {
        OrderStatistics stats = statisticsService.calculate(orders);
        subtitleLabel.setText("По текущей выборке: " + stats.totalOrders() + " из " + totalInDatabase
                + " заказ-нарядов (с учётом поиска и фильтров главного окна)");

        totalValue.setText(String.valueOf(stats.totalOrders()));
        activeValue.setText(String.valueOf(stats.activeOrders()));
        completionValue.setText(stats.completionRatePercent() == null ? "—"
                : String.format(Formats.RU, "%.1f %%", stats.completionRatePercent()));
        revenueValue.setText(Formats.money(stats.revenue()));
        averageCheckValue.setText(Formats.money(stats.averageCheck()));
        minMaxValue.setText(stats.minCost() == null ? "—"
                : Formats.money(stats.minCost()) + " / " + Formats.money(stats.maxCost()));
        durationValue.setText(stats.averageRepairDays() == null ? "—"
                : String.format(Formats.RU, "%.1f дн.", stats.averageRepairDays()));
        topMechanicValue.setText(stats.topMechanic() == null ? "—" : stats.topMechanic());

        statusChart.setData(FXCollections.observableArrayList(stats.ordersByStatus().entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> new PieChart.Data(label(entry), entry.getValue()))
                .toList()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        stats.ordersByMechanic().forEach((name, count) ->
                series.getData().add(new XYChart.Data<>(Formats.shortName(name), count)));
        mechanicChart.getData().setAll(List.of(series));
    }

    @FXML
    private void onClose() {
        ((Stage) totalValue.getScene().getWindow()).close();
    }

    private static String label(Map.Entry<OrderStatus, Long> entry) {
        return entry.getKey().title() + " (" + entry.getValue() + ")";
    }
}
