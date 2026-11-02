package ru.mirea.autoservice.fx.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.mirea.autoservice.fx.AppContext;
import ru.mirea.autoservice.fx.dao.DataAccessException;
import ru.mirea.autoservice.fx.model.Car;
import ru.mirea.autoservice.fx.model.Mechanic;
import ru.mirea.autoservice.fx.model.OrderStatus;
import ru.mirea.autoservice.fx.model.RepairOrder;
import ru.mirea.autoservice.fx.service.BusinessRuleException;
import ru.mirea.autoservice.fx.service.FieldError;
import ru.mirea.autoservice.fx.service.OrderInput;
import ru.mirea.autoservice.fx.service.OrderValidator;
import ru.mirea.autoservice.fx.service.ReferenceDataService;
import ru.mirea.autoservice.fx.service.RepairOrderService;
import ru.mirea.autoservice.fx.service.ValidationException;
import ru.mirea.autoservice.fx.ui.util.Dialogs;
import ru.mirea.autoservice.fx.ui.util.FieldMarks;
import ru.mirea.autoservice.fx.ui.util.Formats;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrderFormController {

    @FXML private Label formTitle;
    @FXML private TextField numberField;
    @FXML private ComboBox<Car> carCombo;
    @FXML private ComboBox<Mechanic> mechanicCombo;
    @FXML private ComboBox<OrderStatus> statusCombo;
    @FXML private DatePicker createdPicker;
    @FXML private DatePicker completedPicker;
    @FXML private TextField costField;
    @FXML private TextArea descriptionArea;
    @FXML private Label descriptionCounter;
    @FXML private VBox errorBox;
    @FXML private Label errorLabel;

    private final RepairOrderService orderService;
    private final ReferenceDataService referenceData;
    private Map<String, Node> fieldNodes;
    private Integer orderId;
    private Integer savedId;

    public OrderFormController(AppContext context) {
        this.orderService = context.orderService();
        this.referenceData = context.referenceData();
    }

    @FXML
    private void initialize() {
        fieldNodes = Map.of(
                OrderValidator.NUMBER, numberField,
                OrderValidator.CAR, carCombo,
                OrderValidator.MECHANIC, mechanicCombo,
                OrderValidator.STATUS, statusCombo,
                OrderValidator.CREATED, createdPicker,
                OrderValidator.COMPLETED, completedPicker,
                OrderValidator.COST, costField,
                OrderValidator.DESCRIPTION, descriptionArea);
        statusCombo.getItems().setAll(OrderStatus.values());
        statusCombo.valueProperty().addListener((obs, old, status) -> onStatusChanged(status));
        Formats.installDateConverter(createdPicker);
        Formats.installDateConverter(completedPicker);
        descriptionArea.textProperty().addListener((obs, old, text) -> descriptionCounter.setText(
                text.strip().length() + " / " + OrderValidator.DESCRIPTION_MAX));
        descriptionCounter.setText("0 / " + OrderValidator.DESCRIPTION_MAX);
        hideErrors();
    }

    public void prepare(RepairOrder order) {
        carCombo.getItems().setAll(referenceData.cars());
        mechanicCombo.getItems().setAll(referenceData.mechanics());
        if (order == null) {
            formTitle.setText("Новый заказ-наряд");
            numberField.setText(orderService.suggestNextNumber());
            statusCombo.setValue(OrderStatus.NEW);
            createdPicker.setValue(LocalDate.now());
            costField.setText("0");
            return;
        }
        orderId = order.id();
        formTitle.setText("Заказ-наряд " + order.orderNumber());
        numberField.setText(order.orderNumber());
        carCombo.getItems().stream().filter(car -> car.id() == order.carId()).findFirst()
                .ifPresent(carCombo::setValue);
        mechanicCombo.getItems().stream().filter(m -> m.id() == order.mechanicId()).findFirst()
                .ifPresent(mechanicCombo::setValue);
        statusCombo.setValue(order.status());
        createdPicker.setValue(order.createdDate());
        completedPicker.setValue(order.completedDate());
        costField.setText(order.cost().toPlainString().replace('.', ','));
        descriptionArea.setText(order.description());
    }

    public Optional<Integer> savedOrderId() {
        return Optional.ofNullable(savedId);
    }

    @FXML
    private void onSave() {
        hideErrors();
        List<FieldError> parseErrors = new ArrayList<>();
        LocalDate created = readDate(createdPicker, OrderValidator.CREATED, "Дата приёма", parseErrors);
        LocalDate completed = readDate(completedPicker, OrderValidator.COMPLETED, "Дата выдачи", parseErrors);
        if (!parseErrors.isEmpty()) {
            showErrors(parseErrors);
            return;
        }
        OrderInput input = new OrderInput(orderId, numberField.getText(), carCombo.getValue(),
                mechanicCombo.getValue(), descriptionArea.getText(), statusCombo.getValue(),
                created, completed, costField.getText());
        try {
            savedId = orderService.save(input);
            stage().close();
        } catch (ValidationException e) {
            showErrors(e.errors());
        } catch (BusinessRuleException | DataAccessException e) {
            Dialogs.operationFailed(stage(), "Заказ-наряд не сохранён", e);
        }
    }

    @FXML
    private void onCancel() {
        stage().close();
    }

    private void onStatusChanged(OrderStatus status) {
        boolean closed = status != null && !status.isActive();
        completedPicker.setDisable(!closed);
        if (!closed) {
            completedPicker.setValue(null);
            completedPicker.getEditor().clear();
            Formats.clearInvalidDate(completedPicker);
        } else if (status == OrderStatus.DONE && completedPicker.getValue() == null) {
            completedPicker.setValue(LocalDate.now());
        }
    }

    private static LocalDate readDate(DatePicker picker, String field, String label, List<FieldError> errors) {
        String invalid = Formats.invalidDateText(picker);
        if (invalid != null) {
            errors.add(new FieldError(field, label + ": «" + invalid
                    + "» — не дата. Введите существующую дату в формате ДД.ММ.ГГГГ."));
            return null;
        }
        try {
            LocalDate date = Formats.parseDate(picker.getEditor().getText());
            picker.setValue(date);
            return date;
        } catch (DateTimeParseException e) {
            errors.add(new FieldError(field, label + ": введите существующую дату в формате ДД.ММ.ГГГГ."));
            return null;
        }
    }

    private void showErrors(List<FieldError> errors) {
        errorLabel.setText(errors.stream().map(error -> "• " + error.message()).collect(Collectors.joining("\n")));
        errorBox.setVisible(true);
        errorBox.setManaged(true);
        Node first = null;
        for (FieldError error : errors) {
            Node node = fieldNodes.get(error.field());
            if (node != null) {
                FieldMarks.set(node, true);
                if (first == null) {
                    first = node;
                }
            }
        }
        if (first != null) {
            first.requestFocus();
        }
        stage().sizeToScene();
    }

    private void hideErrors() {
        errorBox.setVisible(false);
        errorBox.setManaged(false);
        fieldNodes.values().forEach(node -> FieldMarks.set(node, false));
    }

    private Stage stage() {
        return (Stage) formTitle.getScene().getWindow();
    }
}
