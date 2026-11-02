package ru.mirea.autoservice.fx.ui.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mirea.autoservice.fx.dao.DataAccessException;
import ru.mirea.autoservice.fx.service.BusinessRuleException;

public final class Dialogs {

    private static final Logger log = LoggerFactory.getLogger(Dialogs.class);

    private Dialogs() {
    }

    public static void error(Window owner, String header, String message) {
        show(Alert.AlertType.ERROR, owner, "Ошибка", header, message);
    }

    public static void info(Window owner, String header, String message) {
        show(Alert.AlertType.INFORMATION, owner, "Готово", header, message);
    }

    public static void operationFailed(Window owner, String header, RuntimeException e) {
        if (e instanceof BusinessRuleException) {
            log.info("{}: {}", header, e.getMessage());
            show(Alert.AlertType.WARNING, owner, "Операция запрещена", header, e.getMessage());
        } else if (e instanceof DataAccessException) {
            log.error(header, e);
            error(owner, header, e.getMessage());
        } else {
            throw e;
        }
    }

    public static boolean confirm(Window owner, String header, String message, String confirmText) {
        ButtonType confirm = new ButtonType(confirmText, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = create(Alert.AlertType.CONFIRMATION, owner, "Подтверждение", header, message);
        alert.getButtonTypes().setAll(confirm, cancel);
        alert.getDialogPane().lookupButton(confirm).getStyleClass().add("danger");
        ((Button) alert.getDialogPane().lookupButton(confirm)).setDefaultButton(false);
        ((Button) alert.getDialogPane().lookupButton(cancel)).setDefaultButton(true);
        return alert.showAndWait().filter(confirm::equals).isPresent();
    }

    private static void show(Alert.AlertType type, Window owner, String title, String header, String message) {
        create(type, owner, title, header, message).showAndWait();
    }

    private static Alert create(Alert.AlertType type, Window owner, String title, String header, String message) {
        Alert alert = new Alert(type);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.getDialogPane().setMinWidth(460);
        ViewLoader.applyStyles(alert.getDialogPane());
        return alert;
    }
}
