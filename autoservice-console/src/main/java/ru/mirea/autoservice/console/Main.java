package ru.mirea.autoservice.console;

import ru.mirea.autoservice.console.exception.AutoServiceException;
import ru.mirea.autoservice.console.exception.DataAccessException;
import ru.mirea.autoservice.console.ui.ConsoleIO;
import ru.mirea.autoservice.console.ui.ConsoleUI;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        try {
            ApplicationContext context = ApplicationContext.create();
            context.database().checkConnection();
            new ConsoleUI(context, new ConsoleIO()).run();
        } catch (IllegalStateException e) {
            System.err.println("Ошибка конфигурации: " + e.getMessage());
            System.exit(1);
        } catch (DataAccessException e) {
            System.err.println(e.getMessage());
            System.err.println("Проверьте, что PostgreSQL запущен: docker compose up -d --wait");
            System.exit(1);
        } catch (AutoServiceException e) {
            System.err.println("Не удалось запустить приложение: " + e.getMessage());
            System.exit(1);
        }
    }
}
