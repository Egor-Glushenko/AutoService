package ru.mirea.autoservice.fx.ui.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ru.mirea.autoservice.fx.AppContext;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class ViewLoader {

    private static final String STYLESHEET = "/css/app.css";

    public record View<C>(Parent root, C controller) {
    }

    private final AppContext context;

    public ViewLoader(AppContext context) {
        this.context = context;
    }

    public <C> View<C> load(String fxmlName) {
        URL resource = Objects.requireNonNull(ViewLoader.class.getResource("/fxml/" + fxmlName),
                () -> "FXML не найден: " + fxmlName);
        FXMLLoader loader = new FXMLLoader(resource);
        loader.setControllerFactory(this::createController);
        try {
            Parent root = loader.load();
            return new View<>(root, loader.getController());
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось загрузить окно " + fxmlName, e);
        }
    }

    public Stage createWindow(Parent root, String title, Window owner, Modality modality) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initOwner(owner);
        stage.initModality(modality);
        Scene scene = new Scene(root);
        applyStyles(scene);
        stage.setScene(scene);
        return stage;
    }

    public static void applyStyles(Scene scene) {
        scene.getStylesheets().add(stylesheet());
    }

    public static void applyStyles(Parent parent) {
        parent.getStylesheets().add(stylesheet());
    }

    private static String stylesheet() {
        return Objects.requireNonNull(ViewLoader.class.getResource(STYLESHEET)).toExternalForm();
    }

    private Object createController(Class<?> type) {
        try {
            return type.getConstructor(AppContext.class).newInstance(context);
        } catch (NoSuchMethodException e) {
            try {
                return type.getConstructor().newInstance();
            } catch (ReflectiveOperationException inner) {
                throw new IllegalStateException("Не удалось создать контроллер " + type.getName(), inner);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Не удалось создать контроллер " + type.getName(), e);
        }
    }
}
