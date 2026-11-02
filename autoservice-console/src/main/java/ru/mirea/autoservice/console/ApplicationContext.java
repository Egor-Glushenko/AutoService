package ru.mirea.autoservice.console;

import ru.mirea.autoservice.console.config.DatabaseConfig;
import ru.mirea.autoservice.console.export.CsvExporter;
import ru.mirea.autoservice.console.export.DataExporter;
import ru.mirea.autoservice.console.export.ExcelExporter;
import ru.mirea.autoservice.console.repository.CarRepository;
import ru.mirea.autoservice.console.repository.ClientRepository;
import ru.mirea.autoservice.console.repository.MechanicRepository;
import ru.mirea.autoservice.console.repository.RepairOrderRepository;
import ru.mirea.autoservice.console.repository.TableViewRepository;
import ru.mirea.autoservice.console.repository.jdbc.JdbcCarRepository;
import ru.mirea.autoservice.console.repository.jdbc.JdbcClientRepository;
import ru.mirea.autoservice.console.repository.jdbc.JdbcMechanicRepository;
import ru.mirea.autoservice.console.repository.jdbc.JdbcRepairOrderRepository;
import ru.mirea.autoservice.console.repository.jdbc.JdbcTableViewRepository;
import ru.mirea.autoservice.console.service.OrderQueryService;
import ru.mirea.autoservice.console.service.OrderValidator;
import ru.mirea.autoservice.console.service.ReferenceService;
import ru.mirea.autoservice.console.service.RepairOrderService;
import ru.mirea.autoservice.console.service.StatisticsService;
import ru.mirea.autoservice.console.util.DatabaseManager;

import java.time.Clock;
import java.util.List;

public final class ApplicationContext {

    private final DatabaseManager database;
    private final RepairOrderService orderService;
    private final ReferenceService referenceService;
    private final OrderQueryService queryService;
    private final StatisticsService statisticsService;
    private final TableViewRepository tableRepository;
    private final List<DataExporter> exporters;

    private ApplicationContext(DatabaseManager database, RepairOrderService orderService,
                               ReferenceService referenceService, OrderQueryService queryService,
                               StatisticsService statisticsService, TableViewRepository tableRepository,
                               List<DataExporter> exporters) {
        this.database = database;
        this.orderService = orderService;
        this.referenceService = referenceService;
        this.queryService = queryService;
        this.statisticsService = statisticsService;
        this.tableRepository = tableRepository;
        this.exporters = List.copyOf(exporters);
    }

    public static ApplicationContext create() {
        DatabaseManager database = new DatabaseManager(DatabaseConfig.load());

        ClientRepository clients = new JdbcClientRepository(database);
        CarRepository cars = new JdbcCarRepository(database);
        MechanicRepository mechanics = new JdbcMechanicRepository(database);
        RepairOrderRepository orders = new JdbcRepairOrderRepository(database);
        TableViewRepository tables = new JdbcTableViewRepository(database);

        RepairOrderService orderService = new RepairOrderService(orders, cars, mechanics,
                new OrderValidator(Clock.systemDefaultZone()));

        return new ApplicationContext(
                database,
                orderService,
                new ReferenceService(clients, cars, mechanics),
                new OrderQueryService(),
                new StatisticsService(),
                tables,
                List.of(new ExcelExporter(), new CsvExporter()));
    }

    public DatabaseManager database() {
        return database;
    }

    public RepairOrderService orderService() {
        return orderService;
    }

    public ReferenceService referenceService() {
        return referenceService;
    }

    public OrderQueryService queryService() {
        return queryService;
    }

    public StatisticsService statisticsService() {
        return statisticsService;
    }

    public TableViewRepository tableRepository() {
        return tableRepository;
    }

    public List<DataExporter> exporters() {
        return exporters;
    }
}
