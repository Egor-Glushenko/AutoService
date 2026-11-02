package ru.mirea.autoservice.fx.ui.controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mirea.autoservice.fx.AppContext;
import ru.mirea.autoservice.fx.dao.DataAccessException;
import ru.mirea.autoservice.fx.model.Car;
import ru.mirea.autoservice.fx.model.Client;
import ru.mirea.autoservice.fx.model.Mechanic;
import ru.mirea.autoservice.fx.model.OrderStatus;
import ru.mirea.autoservice.fx.model.RepairOrder;
import ru.mirea.autoservice.fx.service.BusinessRuleException;
import ru.mirea.autoservice.fx.service.OrderFilter;
import ru.mirea.autoservice.fx.ui.util.Choice;
import ru.mirea.autoservice.fx.ui.util.Dialogs;
import ru.mirea.autoservice.fx.ui.util.FieldMarks;
import ru.mirea.autoservice.fx.ui.util.Formats;
import ru.mirea.autoservice.fx.ui.util.ViewLoader;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Collator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @FXML private Label connectionLabel;

    @FXML private TableView<RepairOrder> ordersTable;
    @FXML private TableColumn<RepairOrder, String> numberColumn;
    @FXML private TableColumn<RepairOrder, LocalDate> createdColumn;
    @FXML private TableColumn<RepairOrder, String> clientColumn;
    @FXML private TableColumn<RepairOrder, String> carColumn;
    @FXML private TableColumn<RepairOrder, String> plateColumn;
    @FXML private TableColumn<RepairOrder, String> mechanicColumn;
    @FXML private TableColumn<RepairOrder, OrderStatus> statusColumn;
    @FXML private TableColumn<RepairOrder, BigDecimal> costColumn;
    @FXML private TableColumn<RepairOrder, LocalDate> completedColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<Choice<OrderStatus>> statusFilter;
    @FXML private ComboBox<Choice<Integer>> mechanicFilter;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<OrderSortOption> sortCombo;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Label countLabel;

    @FXML private Label detailsHint;
    @FXML private GridPane detailsGrid;
    @FXML private Label detailNumber;
    @FXML private Label detailStatus;
    @FXML private Label detailCreated;
    @FXML private Label detailCompleted;
    @FXML private Label detailDuration;
    @FXML private Label detailCost;
    @FXML private Label detailClient;
    @FXML private Label detailPhone;
    @FXML private Label detailCar;
    @FXML private Label detailYear;
    @FXML private Label detailPlate;
    @FXML private Label detailMechanic;
    @FXML private Label detailSpecialization;
    @FXML private Label detailDescription;

    @FXML private TableView<Client> clientsTable;
    @FXML private TableColumn<Client, String> clientNameColumn;
    @FXML private TableColumn<Client, String> clientPhoneColumn;
    @FXML private TableColumn<Client, String> clientEmailColumn;
    @FXML private TableColumn<Client, Integer> clientCarsColumn;
    @FXML private TableColumn<Client, Integer> clientOrdersColumn;
    @FXML private Label carsTitle;
    @FXML private TableView<Car> carsTable;
    @FXML private TableColumn<Car, String> carBrandColumn;
    @FXML private TableColumn<Car, String> carModelColumn;
    @FXML private TableColumn<Car, Integer> carYearColumn;
    @FXML private TableColumn<Car, String> carPlateColumn;

    private final AppContext context;
    private final ViewLoader views;
    private final Collator collator = Collator.getInstance(Formats.RU);

    private final ObservableList<RepairOrder> orders = FXCollections.observableArrayList();
    private final FilteredList<RepairOrder> filteredOrders = new FilteredList<>(orders);
    private final SortedList<RepairOrder> sortedOrders = new SortedList<>(filteredOrders);

    private final ObservableList<Client> clients = FXCollections.observableArrayList();
    private final ObservableList<Car> cars = FXCollections.observableArrayList();
    private final FilteredList<Car> clientCars = new FilteredList<>(cars, car -> false);

    public MainController(AppContext context) {
        this.context = context;
        this.views = new ViewLoader(context);
    }

    @FXML
    private void initialize() {
        setupOrdersTable();
        setupFilters();
        setupClientsTab();
        showDetails(null);
        connectionLabel.setText("БД: " + context.databaseUrl());
        Platform.runLater(() -> reload(null));
    }

    private void setupOrdersTable() {
        bind(numberColumn, RepairOrder::orderNumber);
        bind(clientColumn, RepairOrder::clientName);
        bind(carColumn, RepairOrder::carTitle);
        bind(plateColumn, RepairOrder::plate);
        bind(mechanicColumn, order -> Formats.shortName(order.mechanicName()));
        createdColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().createdDate()));
        completedColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().completedDate()));
        statusColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().status()));
        costColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().cost()));

        createdColumn.setCellFactory(column -> formattedCell(Formats::date));
        completedColumn.setCellFactory(column -> formattedCell(Formats::date));
        costColumn.setCellFactory(column -> formattedCell(Formats::money));
        statusColumn.setCellFactory(column -> new StatusCell());
        clientColumn.setComparator(collator::compare);
        mechanicColumn.setComparator(collator::compare);
        carColumn.setComparator(collator::compare);

        sortedOrders.comparatorProperty().bind(ordersTable.comparatorProperty());
        ordersTable.setItems(sortedOrders);

        ordersTable.setRowFactory(table -> {
            TableRow<RepairOrder> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openForm(row.getItem());
                }
            });
            return row;
        });
        ordersTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> showDetails(selected));
        var selection = ordersTable.getSelectionModel().selectedItemProperty();
        editButton.disableProperty().bind(selection.isNull());
        deleteButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> selection.get() == null || selection.get().status() == OrderStatus.DONE, selection));
    }

    private void setupFilters() {
        List<Choice<OrderStatus>> statuses = new ArrayList<>();
        statuses.add(new Choice<>(null, "Все статусы"));
        for (OrderStatus status : OrderStatus.values()) {
            statuses.add(new Choice<>(status, status.title()));
        }
        statusFilter.getItems().setAll(statuses);
        statusFilter.getSelectionModel().selectFirst();
        mechanicFilter.getItems().setAll(List.of(allMechanics()));
        mechanicFilter.getSelectionModel().selectFirst();

        Formats.installDateConverter(fromDatePicker);
        Formats.installDateConverter(toDatePicker);
        fromDatePicker.getProperties().addListener((MapChangeListener<Object, Object>) change -> updateCountLabel());
        toDatePicker.getProperties().addListener((MapChangeListener<Object, Object>) change -> updateCountLabel());

        searchField.textProperty().addListener((obs, old, value) -> applyFilter());
        statusFilter.valueProperty().addListener((obs, old, value) -> applyFilter());
        mechanicFilter.valueProperty().addListener((obs, old, value) -> applyFilter());
        fromDatePicker.valueProperty().addListener((obs, old, value) -> applyFilter());
        toDatePicker.valueProperty().addListener((obs, old, value) -> applyFilter());

        sortCombo.getItems().setAll(OrderSortOption.values());
        sortCombo.valueProperty().addListener((obs, old, option) -> applySort(option));
        sortCombo.setValue(OrderSortOption.CREATED_DESC);
    }

    private void setupClientsTab() {
        clientNameColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().fullName()));
        clientPhoneColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().phone()));
        clientEmailColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().email() == null ? "—" : c.getValue().email()));
        clientCarsColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().carCount()));
        clientOrdersColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().orderCount()));
        clientNameColumn.setComparator(collator::compare);
        SortedList<Client> sortedClients = new SortedList<>(clients);
        sortedClients.comparatorProperty().bind(clientsTable.comparatorProperty());
        clientsTable.setItems(sortedClients);

        carBrandColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().brand()));
        carModelColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().model()));
        carYearColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().year()));
        carPlateColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().plate()));
        carsTable.setItems(clientCars);

        clientsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, client) -> {
            clientCars.setPredicate(car -> client != null && car.clientId() == client.id());
            carsTitle.setText(client == null ? "Автомобили клиента" : "Автомобили клиента: " + client.fullName());
        });
    }

    private void reload(Integer selectId) {
        Integer keepOrder = selectId != null ? selectId : selectedOrderId();
        Client selectedClient = clientsTable.getSelectionModel().getSelectedItem();
        try {
            List<RepairOrder> loadedOrders = context.orderService().findAll();
            List<Mechanic> mechanics = context.referenceData().mechanics();
            List<Client> loadedClients = context.referenceData().clients();
            List<Car> loadedCars = context.referenceData().cars();

            orders.setAll(loadedOrders);
            updateMechanicFilter(mechanics);
            cars.setAll(loadedCars);
            clients.setAll(loadedClients);
            selectOrder(keepOrder);
            if (selectedClient != null) {
                clientsTable.getItems().stream().filter(c -> c.id() == selectedClient.id()).findFirst()
                        .ifPresent(c -> clientsTable.getSelectionModel().select(c));
            }
            setConnectionState(true);
        } catch (DataAccessException e) {
            setConnectionState(false);
            Dialogs.operationFailed(window(), "Не удалось загрузить данные", e);
        }
        updateCountLabel();
    }

    private void updateMechanicFilter(List<Mechanic> mechanics) {
        Integer selected = mechanicFilter.getValue() == null ? null : mechanicFilter.getValue().value();
        List<Choice<Integer>> items = new ArrayList<>();
        items.add(allMechanics());
        mechanics.forEach(m -> items.add(new Choice<>(m.id(), Formats.shortName(m.fullName()))));
        mechanicFilter.getItems().setAll(items);
        mechanicFilter.setValue(items.stream()
                .filter(choice -> Objects.equals(choice.value(), selected))
                .findFirst().orElse(items.get(0)));
    }

    private static Choice<Integer> allMechanics() {
        return new Choice<>(null, "Все механики");
    }

    private void applyFilter() {
        if (isInvalidRange()) {
            filteredOrders.setPredicate(order -> false);
        } else {
            filteredOrders.setPredicate(new OrderFilter(
                    searchField.getText(),
                    statusFilter.getValue() == null ? null : statusFilter.getValue().value(),
                    mechanicFilter.getValue() == null ? null : mechanicFilter.getValue().value(),
                    fromDatePicker.getValue(), toDatePicker.getValue()));
        }
        updateCountLabel();
    }

    private boolean isInvalidRange() {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();
        return from != null && to != null && from.isAfter(to);
    }

    private void applySort(OrderSortOption option) {
        if (option == null) {
            return;
        }
        TableColumn<RepairOrder, ?> column = switch (option.field()) {
            case CREATED -> createdColumn;
            case COST -> costColumn;
            case CLIENT -> clientColumn;
            case NUMBER -> numberColumn;
        };
        column.setSortType(option.ascending() ? TableColumn.SortType.ASCENDING : TableColumn.SortType.DESCENDING);
        ordersTable.getSortOrder().setAll(List.of(column));
    }

    @FXML
    private void onResetFilters() {
        searchField.clear();
        statusFilter.getSelectionModel().selectFirst();
        mechanicFilter.getSelectionModel().selectFirst();
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        fromDatePicker.getEditor().clear();
        toDatePicker.getEditor().clear();
        Formats.clearInvalidDate(fromDatePicker);
        Formats.clearInvalidDate(toDatePicker);
        sortCombo.setValue(OrderSortOption.CREATED_DESC);
        applySort(OrderSortOption.CREATED_DESC);
    }

    private void updateCountLabel() {
        boolean invalidRange = isInvalidRange();
        FieldMarks.set(fromDatePicker, invalidRange || Formats.invalidDateText(fromDatePicker) != null);
        FieldMarks.set(toDatePicker, invalidRange || Formats.invalidDateText(toDatePicker) != null);
        String message = null;
        if (Formats.invalidDateText(fromDatePicker) != null || Formats.invalidDateText(toDatePicker) != null) {
            message = "Дата периода введена неверно — используйте формат ДД.ММ.ГГГГ. "
                    + "Фильтр применяется по последней корректной дате.";
        } else if (invalidRange) {
            message = "Начальная дата периода позже конечной — исправьте период.";
        }
        FieldMarks.set(countLabel, message != null);
        countLabel.setText(message != null ? message
                : "Показано заказ-нарядов: " + filteredOrders.size() + " из " + orders.size());
    }

    @FXML
    private void onAdd() {
        openForm(null);
    }

    @FXML
    private void onEdit() {
        RepairOrder selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openForm(selected);
        }
    }

    private void openForm(RepairOrder order) {
        ViewLoader.View<OrderFormController> view = views.load("order-form.fxml");
        try {
            view.controller().prepare(order);
        } catch (DataAccessException e) {
            Dialogs.operationFailed(window(), "Не удалось открыть форму заказ-наряда", e);
            return;
        }
        String title = order == null ? "Новый заказ-наряд" : "Редактирование " + order.orderNumber();
        Stage stage = views.createWindow(view.root(), title, window(), Modality.WINDOW_MODAL);
        stage.setMinWidth(560);
        stage.setMinHeight(520);
        stage.showAndWait();
        view.controller().savedOrderId().ifPresent(this::reload);
    }

    @FXML
    private void onDelete() {
        RepairOrder order = ordersTable.getSelectionModel().getSelectedItem();
        if (order == null) {
            return;
        }
        boolean confirmed = Dialogs.confirm(window(), "Удалить заказ-наряд " + order.orderNumber() + "?",
                "Клиент: " + order.clientName()
                        + "\nАвтомобиль: " + order.carTitle() + ", " + order.plate()
                        + "\nСтатус: " + order.status().title()
                        + "\n\nУдалённую запись нельзя восстановить.",
                "Удалить");
        if (!confirmed) {
            return;
        }
        try {
            context.orderService().delete(order);
            reload(null);
        } catch (BusinessRuleException | DataAccessException e) {
            Dialogs.operationFailed(window(), "Заказ-наряд не удалён", e);
        }
    }

    @FXML
    private void onRefresh() {
        reload(null);
    }

    @FXML
    private void onStatistics() {
        ViewLoader.View<StatisticsController> view = views.load("statistics.fxml");
        view.controller().show(List.copyOf(sortedOrders), orders.size());
        Stage stage = views.createWindow(view.root(), "Статистика автосервиса", window(), Modality.WINDOW_MODAL);
        stage.setMinWidth(760);
        stage.setMinHeight(560);
        stage.showAndWait();
    }

    @FXML
    private void onExport() {
        if (sortedOrders.isEmpty()) {
            Dialogs.info(window(), "Нечего экспортировать",
                    "В таблице нет записей. Измените условия поиска или фильтры.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Экспорт заказ-нарядов в CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV для Excel (*.csv)", "*.csv"));
        chooser.setInitialFileName("zakaz-naryady-" + LocalDate.now() + ".csv");
        File file = chooser.showSaveDialog(window());
        if (file == null) {
            return;
        }
        Path path = file.toPath();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            path = Path.of(file.getPath() + ".csv");
            if (Files.exists(path) && !Dialogs.confirm(window(), "Файл «" + path.getFileName() + "» уже существует",
                    "Заменить его новыми данными?", "Заменить")) {
                return;
            }
        }
        List<RepairOrder> rows = List.copyOf(sortedOrders);
        try {
            context.csvExport().export(rows, path);
            Dialogs.info(window(), "Экспорт выполнен",
                    "Выгружено записей: " + rows.size() + "\nФайл: " + path.toAbsolutePath());
        } catch (IOException e) {
            log.error("Ошибка экспорта в {}", path, e);
            Dialogs.error(window(), "Экспорт не выполнен",
                    "Не удалось записать файл «" + path.getFileName() + "». Проверьте, что файл не открыт "
                            + "в другой программе и в выбранную папку разрешена запись.");
        }
    }

    private void showDetails(RepairOrder order) {
        boolean empty = order == null;
        detailsHint.setVisible(empty);
        detailsHint.setManaged(empty);
        detailsGrid.setVisible(!empty);
        detailsGrid.setManaged(!empty);
        if (empty) {
            return;
        }
        detailNumber.setText(order.orderNumber());
        detailStatus.setText(order.status() == OrderStatus.DONE
                ? order.status().title() + " (удаление недоступно, можно отменить)"
                : order.status().title());
        detailCreated.setText(Formats.date(order.createdDate()));
        detailCompleted.setText(Formats.date(order.completedDate()));
        detailDuration.setText(order.repairDays().isPresent() ? order.repairDays().getAsLong() + " дн." : "—");
        detailCost.setText(Formats.money(order.cost()));
        detailClient.setText(order.clientName());
        detailPhone.setText(order.clientPhone());
        detailCar.setText(order.carTitle());
        detailYear.setText(String.valueOf(order.carYear()));
        detailPlate.setText(order.plate());
        detailMechanic.setText(order.mechanicName());
        detailSpecialization.setText(order.mechanicSpecialization());
        detailDescription.setText(order.description());
    }

    private Integer selectedOrderId() {
        RepairOrder selected = ordersTable.getSelectionModel().getSelectedItem();
        return selected == null ? null : selected.id();
    }

    private void selectOrder(Integer id) {
        if (id == null) {
            ordersTable.getSelectionModel().clearSelection();
            return;
        }
        sortedOrders.stream().filter(order -> order.id() == id).findFirst().ifPresentOrElse(order -> {
            ordersTable.getSelectionModel().select(order);
            ordersTable.scrollTo(order);
        }, () -> ordersTable.getSelectionModel().clearSelection());
    }

    private void setConnectionState(boolean connected) {
        connectionLabel.getStyleClass().removeAll("badge-ok", "badge-error");
        connectionLabel.getStyleClass().add(connected ? "badge-ok" : "badge-error");
        connectionLabel.setText((connected ? "Подключено: " : "Нет подключения: ") + context.databaseUrl());
    }

    private Window window() {
        return ordersTable.getScene() == null ? null : ordersTable.getScene().getWindow();
    }

    private static <S> void bind(TableColumn<S, String> column, Function<S, String> getter) {
        column.setCellValueFactory(c -> new ReadOnlyStringWrapper(getter.apply(c.getValue())));
    }

    private static <S, T> TableCell<S, T> formattedCell(Function<T, String> formatter) {
        return new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : formatter.apply(item));
            }
        };
    }

    private static final class StatusCell extends TableCell<RepairOrder, OrderStatus> {
        @Override
        protected void updateItem(OrderStatus status, boolean empty) {
            super.updateItem(status, empty);
            getStyleClass().removeIf(style -> style.startsWith("status-"));
            if (empty || status == null) {
                setText(null);
                return;
            }
            setText(status.title());
            getStyleClass().add("status-" + status.name().toLowerCase(Locale.ROOT).replace('_', '-'));
        }
    }
}
