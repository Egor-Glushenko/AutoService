package ru.mirea.autoservice.fx;

import ru.mirea.autoservice.fx.config.DatabaseConfig;
import ru.mirea.autoservice.fx.dao.CarDao;
import ru.mirea.autoservice.fx.dao.ClientDao;
import ru.mirea.autoservice.fx.dao.ConnectionFactory;
import ru.mirea.autoservice.fx.dao.MechanicDao;
import ru.mirea.autoservice.fx.dao.RepairOrderDao;
import ru.mirea.autoservice.fx.service.CsvExportService;
import ru.mirea.autoservice.fx.service.OrderValidator;
import ru.mirea.autoservice.fx.service.ReferenceDataService;
import ru.mirea.autoservice.fx.service.RepairOrderService;
import ru.mirea.autoservice.fx.service.StatisticsService;

import java.time.Clock;

public final class AppContext {

    private final String databaseUrl;
    private final RepairOrderService orderService;
    private final ReferenceDataService referenceData;
    private final StatisticsService statistics;
    private final CsvExportService csvExport;

    private AppContext(String databaseUrl, RepairOrderService orderService, ReferenceDataService referenceData,
                       StatisticsService statistics, CsvExportService csvExport) {
        this.databaseUrl = databaseUrl;
        this.orderService = orderService;
        this.referenceData = referenceData;
        this.statistics = statistics;
        this.csvExport = csvExport;
    }

    public static AppContext create() {
        ConnectionFactory connections = new ConnectionFactory(DatabaseConfig.load());
        RepairOrderDao orderDao = new RepairOrderDao(connections);
        CarDao carDao = new CarDao(connections);
        MechanicDao mechanicDao = new MechanicDao(connections);
        ClientDao clientDao = new ClientDao(connections);
        return new AppContext(
                connections.describe(),
                new RepairOrderService(orderDao, carDao, mechanicDao, new OrderValidator(Clock.systemDefaultZone())),
                new ReferenceDataService(clientDao, carDao, mechanicDao),
                new StatisticsService(),
                new CsvExportService());
    }

    public String databaseUrl() {
        return databaseUrl;
    }

    public RepairOrderService orderService() {
        return orderService;
    }

    public ReferenceDataService referenceData() {
        return referenceData;
    }

    public StatisticsService statistics() {
        return statistics;
    }

    public CsvExportService csvExport() {
        return csvExport;
    }
}
