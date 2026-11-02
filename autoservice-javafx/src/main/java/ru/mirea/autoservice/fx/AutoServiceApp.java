package ru.mirea.autoservice.fx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mirea.autoservice.fx.ui.controller.MainController;
import ru.mirea.autoservice.fx.ui.util.Dialogs;
import ru.mirea.autoservice.fx.ui.util.ViewLoader;

public class AutoServiceApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(AutoServiceApp.class);

    @Override
    public void start(Stage stage) {
        Thread.currentThread().setUncaughtExceptionHandler((thread, error) -> {
            log.error("Необработанная ошибка в потоке интерфейса", error);
            Platform.runLater(() -> Dialogs.error(null, "Непредвиденная ошибка",
                    "Операция не выполнена. Подробности выведены в журнал приложения (консоль)."));
        });

        AppContext context;
        try {
            context = AppContext.create();
        } catch (IllegalStateException e) {
            log.error("Ошибка конфигурации", e);
            Dialogs.error(null, "Ошибка конфигурации", e.getMessage());
            Platform.exit();
            return;
        }

        ViewLoader.View<MainController> main = new ViewLoader(context).load("main.fxml");
        Scene scene = new Scene(main.root(), 1280, 780);
        ViewLoader.applyStyles(scene);
        stage.setTitle("Автосервис - заказ-наряды");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
