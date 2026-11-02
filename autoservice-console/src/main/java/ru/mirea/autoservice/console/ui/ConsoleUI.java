package ru.mirea.autoservice.console.ui;

import ru.mirea.autoservice.console.ApplicationContext;
import ru.mirea.autoservice.console.exception.AutoServiceException;
import ru.mirea.autoservice.console.exception.BusinessException;
import ru.mirea.autoservice.console.exception.EntityNotFoundException;
import ru.mirea.autoservice.console.exception.FieldError;
import ru.mirea.autoservice.console.exception.ValidationException;
import ru.mirea.autoservice.console.export.DataExporter;
import ru.mirea.autoservice.console.model.Client;
import ru.mirea.autoservice.console.model.OrderInput;
import ru.mirea.autoservice.console.model.OrderStatistics;
import ru.mirea.autoservice.console.model.OrderStatus;
import ru.mirea.autoservice.console.model.RepairOrder;
import ru.mirea.autoservice.console.service.OrderQueryService;
import ru.mirea.autoservice.console.service.OrderSortOption;
import ru.mirea.autoservice.console.service.ReferenceService;
import ru.mirea.autoservice.console.service.RepairOrderService;
import ru.mirea.autoservice.console.service.SearchField;
import ru.mirea.autoservice.console.service.StatisticsService;
import ru.mirea.autoservice.console.util.Formats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.OptionalInt;

public class ConsoleUI {

    private final ApplicationContext context;
    private final ConsoleIO io;
    private final TablePrinter tables;
    private final RepairOrderService orderService;
    private final ReferenceService reference;
    private final OrderQueryService queryService;
    private final StatisticsService statisticsService;

    public ConsoleUI(ApplicationContext context, ConsoleIO io) {
        this.context = context;
        this.io = io;
        this.tables = new TablePrinter(io);
        this.orderService = context.orderService();
        this.reference = context.referenceService();
        this.queryService = context.queryService();
        this.statisticsService = context.statisticsService();
    }

    public void run() {
        io.println();
        io.println("Подключение к базе данных: " + context.database().describe());
        while (true) {
            printMainMenu();
            try {
                int choice = io.readChoice("Выберите действие: ", 0, 10);
                if (choice == 0) {
                    io.println("Работа завершена.");
                    return;
                }
                dispatch(choice);
            } catch (InputClosedException e) {
                io.println();
                io.println("Ввод завершён. Работа завершена.");
                return;
            } catch (ValidationException e) {
                printValidationErrors(e);
            } catch (AutoServiceException e) {
                io.error(e.getMessage());
            } catch (RuntimeException e) {
                io.error("Непредвиденная ошибка: " + e.getMessage());
            }
            io.println();
        }
    }

    private void dispatch(int choice) {
        switch (choice) {
            case 1 -> clientsMenu();
            case 2 -> carsMenu();
            case 3 -> mechanicsMenu();
            case 4 -> ordersMenu();
            case 5 -> searchMenu();
            case 6 -> filterMenu();
            case 7 -> sortMenu();
            case 8 -> showStatistics();
            case 9 -> exportMenu();
            case 10 -> printDatabaseTables();
            default -> io.error("Неизвестный пункт меню.");
        }
    }

    private void printMainMenu() {
        io.println("=".repeat(78));
        io.println("               АВТОСЕРВИС — УЧЁТ ЗАКАЗ-НАРЯДОВ");
        io.println("               Этап 1: консольное приложение (JDBC)");
        io.println("=".repeat(78));
        io.println(" 1. Клиенты");
        io.println(" 2. Автомобили");
        io.println(" 3. Механики");
        io.println(" 4. Заказ-наряды (создание, просмотр, изменение, удаление)");
        io.println(" 5. Поиск заказ-нарядов");
        io.println(" 6. Фильтрация заказ-нарядов");
        io.println(" 7. Сортировка заказ-нарядов");
        io.println(" 8. Статистика");
        io.println(" 9. Экспорт данных (Excel / CSV)");
        io.println("10. Вывести таблицы базы данных");
        io.println(" 0. Выход");
        io.println("-".repeat(78));
    }

    private void clientsMenu() {
        while (true) {
            io.println();
            io.println("--- КЛИЕНТЫ ---");
            io.println("1. Список клиентов");
            io.println("2. Поиск клиента по ФИО или телефону");
            io.println("3. Автомобили клиента");
            io.println("0. Назад");
            int choice = io.readChoice("Выберите действие: ", 0, 3);
            if (choice == 0) {
                return;
            }
            switch (choice) {
                case 1 -> perform(() -> tables.printClients(reference.clients()));
                case 2 -> perform(this::findClients);
                case 3 -> perform(this::showClientCars);
                default -> {
                }
            }
        }
    }

    private void findClients() {
        String query = io.readRequired("Введите часть ФИО или телефон: ");
        tables.printClients(reference.findClients(query));
    }

    private void showClientCars() {
        Integer id = askId("ID клиента (0 — отмена): ");
        if (id == null) {
            return;
        }
        Client client = reference.client(id).orElseThrow(
                () -> new EntityNotFoundException("Клиент с ID " + id + " не найден."));
        io.println("Автомобили клиента " + client.fullName() + ":");
        tables.printCars(reference.carsOfClient(id));
    }

    private void carsMenu() {
        while (true) {
            io.println();
            io.println("--- АВТОМОБИЛИ ---");
            io.println("1. Список автомобилей");
            io.println("2. Поиск по марке, модели, госномеру или владельцу");
            io.println("0. Назад");
            int choice = io.readChoice("Выберите действие: ", 0, 2);
            if (choice == 0) {
                return;
            }
            switch (choice) {
                case 1 -> perform(() -> tables.printCars(reference.cars()));
                case 2 -> perform(() -> {
                    String query = io.readRequired("Строка поиска: ");
                    tables.printCars(reference.findCars(query));
                });
                default -> {
                }
            }
        }
    }

    private void mechanicsMenu() {
        while (true) {
            io.println();
            io.println("--- МЕХАНИКИ ---");
            io.println("1. Список механиков");
            io.println("2. Поиск по ФИО или специализации");
            io.println("0. Назад");
            int choice = io.readChoice("Выберите действие: ", 0, 2);
            if (choice == 0) {
                return;
            }
            switch (choice) {
                case 1 -> perform(() -> tables.printMechanics(reference.mechanics()));
                case 2 -> perform(() -> {
                    String query = io.readRequired("Строка поиска: ");
                    tables.printMechanics(reference.findMechanics(query));
                });
                default -> {
                }
            }
        }
    }

    private void ordersMenu() {
        while (true) {
            io.println();
            io.println("--- ЗАКАЗ-НАРЯДЫ ---");
            io.println("1. Список всех заказ-нарядов");
            io.println("2. Найти по ID");
            io.println("3. Создать");
            io.println("4. Изменить");
            io.println("5. Удалить");
            io.println("0. Назад");
            int choice = io.readChoice("Выберите действие: ", 0, 5);
            if (choice == 0) {
                return;
            }
            switch (choice) {
                case 1 -> perform(() -> tables.printOrders(orderService.findAll()));
                case 2 -> perform(this::showOrderById);
                case 3 -> perform(this::createOrder);
                case 4 -> perform(this::updateOrder);
                case 5 -> perform(this::deleteOrder);
                default -> {
                }
            }
        }
    }

    private void showOrderById() {
        Integer id = askId("ID заказ-наряда (0 — отмена): ");
        if (id == null) {
            return;
        }
        tables.printOrderCard(orderService.getRequired(id));
    }

    private void createOrder() {
        io.println();
        io.println("--- НОВЫЙ ЗАКАЗ-НАРЯД ---");
        String number = io.readRequiredWithDefault("Номер заказ-наряда", orderService.suggestNextNumber());

        tables.printCarChoices(reference.cars());
        Integer carId = askCarId(null);
        if (carId == null) {
            io.println("Создание отменено.");
            return;
        }

        tables.printMechanicChoices(reference.mechanics());
        Integer mechanicId = askMechanicId(null);
        if (mechanicId == null) {
            io.println("Создание отменено.");
            return;
        }

        String description = io.readRequired("Описание работ: ");
        OrderStatus status = askStatus(OrderStatus.NEW);
        LocalDate created = askDate("Дата приёма", LocalDate.now(), true);
        LocalDate completed = askCompletedDate(status, null);
        String cost = io.readLineWithDefault("Стоимость, ₽ (0, если неизвестна)", "0");

        OrderInput input = new OrderInput(null, number, carId, mechanicId, description, status,
                created, completed, cost);
        int id = orderService.create(input);
        io.println("Создан заказ-наряд " + number + " (ID " + id + ").");
    }

    private void updateOrder() {
        Integer id = askId("ID заказ-наряда (0 — отмена): ");
        if (id == null) {
            return;
        }
        RepairOrder current = orderService.getRequired(id);
        tables.printOrderCard(current);
        io.println("Пустое поле — оставить текущее значение.");

        String number = io.readRequiredWithDefault("Номер заказ-наряда", current.orderNumber());

        tables.printCarChoices(reference.cars());
        Integer carId = askCarId(current.carId());
        if (carId == null) {
            io.println("Изменение отменено.");
            return;
        }

        tables.printMechanicChoices(reference.mechanics());
        Integer mechanicId = askMechanicId(current.mechanicId());
        if (mechanicId == null) {
            io.println("Изменение отменено.");
            return;
        }

        String description = io.readRequiredWithDefault("Описание работ", current.description());
        OrderStatus status = askStatus(current.status());
        LocalDate created = askDate("Дата приёма", current.createdDate(), true);
        LocalDate completed = askCompletedDate(status, current.completedDate());
        String cost = io.readLineWithDefault("Стоимость, ₽",
                current.cost().toPlainString().replace('.', ','));

        orderService.update(id, new OrderInput(id, number, carId, mechanicId, description, status,
                created, completed, cost));
        io.println("Заказ-наряд " + number + " сохранён.");
    }

    private void deleteOrder() {
        Integer id = askId("ID заказ-наряда (0 — отмена): ");
        if (id == null) {
            return;
        }
        RepairOrder order = orderService.getRequired(id);
        tables.printOrderCard(order);
        if (!io.readYesNo("Удалить заказ-наряд? (y/n): ", false)) {
            io.println("Удаление отменено.");
            return;
        }
        orderService.delete(id);
        io.println("Заказ-наряд " + order.orderNumber() + " удалён.");
    }

    private void searchMenu() {
        while (true) {
            io.println();
            io.println("--- ПОИСК ЗАКАЗ-НАРЯДОВ ---");
            SearchField[] fields = SearchField.values();
            for (int i = 0; i < fields.length; i++) {
                io.println((i + 1) + ". " + capitalize(fields[i].title()));
            }
            io.println("0. Назад");
            int choice = io.readChoice("Выберите действие: ", 0, fields.length);
            if (choice == 0) {
                return;
            }
            SearchField field = fields[choice - 1];
            perform(() -> {
                String query = io.readRequired("Строка поиска: ");
                List<RepairOrder> found = queryService.search(orderService.findAll(), field, query);
                tables.printOrders(found);
            });
        }
    }

    private void filterMenu() {
        while (true) {
            io.println();
            io.println("--- ФИЛЬТРАЦИЯ ЗАКАЗ-НАРЯДОВ ---");
            io.println("1. По статусу");
            io.println("2. По механику");
            io.println("3. По периоду даты приёма");
            io.println("4. По клиенту");
            io.println("0. Назад");
            int choice = io.readChoice("Выберите действие: ", 0, 4);
            if (choice == 0) {
                return;
            }
            switch (choice) {
                case 1 -> perform(this::filterByStatus);
                case 2 -> perform(this::filterByMechanic);
                case 3 -> perform(this::filterByPeriod);
                case 4 -> perform(this::filterByClient);
                default -> {
                }
            }
        }
    }

    private void filterByStatus() {
        OrderStatus status = askStatus(null);
        tables.printOrders(queryService.filter(orderService.findAll(), status, null, null, null));
    }

    private void filterByMechanic() {
        tables.printMechanicChoices(reference.mechanics());
        while (true) {
            OptionalInt value = io.readInt("ID механика (0 — отмена): ");
            if (value.isEmpty()) {
                io.error("Введите ID механика.");
                continue;
            }
            int id = value.getAsInt();
            if (id == 0) {
                return;
            }
            if (reference.mechanic(id).isEmpty()) {
                io.error("Механик с ID " + id + " не найден.");
                continue;
            }
            tables.printOrders(queryService.filter(orderService.findAll(), null, id, null, null));
            return;
        }
    }

    private void filterByPeriod() {
        LocalDate from = askDate("Дата приёма с (пусто — без ограничения)", null, false);
        LocalDate to = askDate("Дата приёма по (пусто — без ограничения)", null, false);
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException("Начальная дата периода позже конечной.");
        }
        tables.printOrders(queryService.filter(orderService.findAll(), null, null, from, to));
    }

    private void filterByClient() {
        String query = io.readRequired("Введите часть ФИО клиента: ");
        tables.printOrders(queryService.filterByClient(orderService.findAll(), query));
    }

    private void sortMenu() {
        OrderSortOption[] options = OrderSortOption.values();
        while (true) {
            io.println();
            io.println("--- СОРТИРОВКА ЗАКАЗ-НАРЯДОВ ---");
            for (int i = 0; i < options.length; i++) {
                io.println((i + 1) + ". " + options[i].title());
            }
            io.println("0. Назад");
            int choice = io.readChoice("Выберите действие: ", 0, options.length);
            if (choice == 0) {
                return;
            }
            OrderSortOption option = options[choice - 1];
            perform(() -> tables.printOrders(queryService.sort(orderService.findAll(), option)));
        }
    }

    private void showStatistics() {
        List<RepairOrder> all = orderService.findAll();
        OrderStatistics stats = statisticsService.calculate(all, reference.clientCount(),
                reference.carCount(), reference.mechanicCount());

        io.println();
        io.println("=".repeat(78));
        io.println("                      СТАТИСТИКА АВТОСЕРВИСА");
        io.println("=".repeat(78));
        io.println("Всего клиентов:                  " + stats.totalClients());
        io.println("Всего автомобилей:               " + stats.totalCars());
        io.println("Всего механиков:                 " + stats.totalMechanics());
        io.println("Всего заказ-нарядов:             " + stats.totalOrders());
        io.println("Активные (новые и в работе):     " + stats.activeOrders());
        io.println("Выполненные:                     " + stats.doneOrders());
        io.println("Отменённые:                      " + stats.cancelledOrders());
        io.println("Доля выполненных среди закрытых: " + (stats.completionRatePercent() == null
                ? "—" : String.format(Formats.RU, "%.1f %%", stats.completionRatePercent())));
        io.println("Выручка по выполненным:          " + Formats.money(stats.revenue()));
        io.println("Средний чек:                     " + Formats.money(stats.averageCheck()));
        io.println("Минимальная стоимость:           " + Formats.money(stats.minCost()));
        io.println("Максимальная стоимость:          " + Formats.money(stats.maxCost()));
        io.println("Средний срок ремонта:            " + (stats.averageRepairDays() == null
                ? "—" : String.format(Formats.RU, "%.1f дн.", stats.averageRepairDays())));
        io.println("Больше всего заказов у:          " + (stats.topMechanic() == null
                ? "—" : stats.topMechanic()));
        io.println("-".repeat(78));
        io.println("Заказы по статусам:");
        for (OrderStatus status : OrderStatus.values()) {
            io.println("  " + status.title() + ": " + stats.count(status));
        }
        io.println("Заказы по механикам:");
        stats.ordersByMechanic().forEach((name, count) -> io.println("  " + name + ": " + count));
        io.println("=".repeat(78));
    }

    private void exportMenu() {
        List<DataExporter> exporters = context.exporters();
        while (true) {
            io.println();
            io.println("--- ЭКСПОРТ ДАННЫХ ---");
            for (int i = 0; i < exporters.size(); i++) {
                io.println((i + 1) + ". " + exporters.get(i).title());
            }
            io.println("0. Назад");
            int choice = io.readChoice("Выберите действие: ", 0, exporters.size());
            if (choice == 0) {
                return;
            }
            DataExporter exporter = exporters.get(choice - 1);
            String defaultPath = "exports/zakaz-naryady-" + LocalDate.now() + "." + exporter.extension();
            String pathText = io.readLineWithDefault("Путь к файлу", defaultPath);
            perform(() -> {
                Path path;
                try {
                    path = Path.of(pathText);
                } catch (InvalidPathException e) {
                    throw new AutoServiceException("Некорректный путь к файлу: " + pathText, e);
                }
                if (Files.exists(path) && !io.readYesNo(
                        "Файл «" + path.getFileName() + "» уже существует. Заменить? (y/n): ", false)) {
                    io.println("Экспорт отменён.");
                    return;
                }
                List<RepairOrder> rows = orderService.findAll();
                try {
                    exporter.export(rows, path);
                    io.println("Экспортировано записей: " + rows.size());
                    io.println("Файл: " + path.toAbsolutePath());
                } catch (IOException e) {
                    throw new AutoServiceException("Не удалось записать файл «"
                            + path.getFileName() + "»: " + e.getMessage(), e);
                }
            });
        }
    }

    private void printDatabaseTables() {
        tables.printSnapshot(context.tableRepository().clients());
        tables.printSnapshot(context.tableRepository().cars());
        tables.printSnapshot(context.tableRepository().mechanics());
        tables.printSnapshot(context.tableRepository().repairOrders());
    }

    private Integer askId(String prompt) {
        while (true) {
            OptionalInt value = io.readInt(prompt);
            if (value.isEmpty()) {
                io.error("Введите ID.");
                continue;
            }
            int id = value.getAsInt();
            if (id == 0) {
                return null;
            }
            if (id < 0) {
                io.error("ID должен быть положительным числом.");
                continue;
            }
            return id;
        }
    }

    private Integer askCarId(Integer current) {
        while (true) {
            String prompt = "ID автомобиля" + (current == null ? "" : " [" + current + "]") + " (0 — отмена): ";
            OptionalInt value = io.readInt(prompt);
            if (value.isEmpty()) {
                if (current != null) {
                    return current;
                }
                io.error("Введите ID автомобиля или 0 для отмены.");
                continue;
            }
            int id = value.getAsInt();
            if (id == 0) {
                return null;
            }
            if (reference.car(id).isPresent()) {
                return id;
            }
            io.error("Автомобиль с ID " + id + " не найден.");
        }
    }

    private Integer askMechanicId(Integer current) {
        while (true) {
            String prompt = "ID механика" + (current == null ? "" : " [" + current + "]") + " (0 — отмена): ";
            OptionalInt value = io.readInt(prompt);
            if (value.isEmpty()) {
                if (current != null) {
                    return current;
                }
                io.error("Введите ID механика или 0 для отмены.");
                continue;
            }
            int id = value.getAsInt();
            if (id == 0) {
                return null;
            }
            if (reference.mechanic(id).isPresent()) {
                return id;
            }
            io.error("Механик с ID " + id + " не найден.");
        }
    }

    private OrderStatus askStatus(OrderStatus current) {
        OrderStatus[] values = OrderStatus.values();
        io.println("Статусы:");
        for (int i = 0; i < values.length; i++) {
            io.println("  " + (i + 1) + ". " + values[i].title());
        }
        while (true) {
            OptionalInt value = io.readInt("Статус" + (current == null ? "" : " [" + current.title() + "]") + ": ");
            if (value.isEmpty()) {
                if (current != null) {
                    return current;
                }
                io.error("Выберите статус.");
                continue;
            }
            int index = value.getAsInt();
            if (index >= 1 && index <= values.length) {
                return values[index - 1];
            }
            io.error("Ошибка: выберите номер от 1 до " + values.length + ".");
        }
    }

    private LocalDate askDate(String label, LocalDate defaultValue, boolean required) {
        while (true) {
            String suffix = defaultValue == null ? "" : " [" + Formats.date(defaultValue) + "]";
            String text = io.readLine(label + suffix + ": ");
            if (text.isEmpty()) {
                if (defaultValue != null) {
                    return defaultValue;
                }
                if (!required) {
                    return null;
                }
                io.error("Дата обязательна.");
                continue;
            }
            try {
                return Formats.parseDate(text);
            } catch (DateTimeParseException e) {
                io.error("Ошибка: введите дату в формате ДД.ММ.ГГГГ.");
            }
        }
    }

    private LocalDate askCompletedDate(OrderStatus status, LocalDate current) {
        if (status == null || status.isActive()) {
            return null;
        }
        boolean required = status == OrderStatus.DONE;
        return askDate(required ? "Дата выдачи (обязательна)" : "Дата выдачи (можно оставить пустой)",
                current, required);
    }

    private void perform(Operation operation) {
        try {
            operation.run();
        } catch (InputClosedException e) {
            throw e;
        } catch (ValidationException e) {
            printValidationErrors(e);
        } catch (AutoServiceException e) {
            io.error(e.getMessage());
        } catch (RuntimeException e) {
            io.error("Непредвиденная ошибка: " + e.getMessage());
        }
    }

    private void printValidationErrors(ValidationException e) {
        io.println("[!] Данные не сохранены. Исправьте ошибки:");
        for (FieldError error : e.errors()) {
            io.println("    • " + error.message());
        }
    }

    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    @FunctionalInterface
    private interface Operation {
        void run();
    }
}
