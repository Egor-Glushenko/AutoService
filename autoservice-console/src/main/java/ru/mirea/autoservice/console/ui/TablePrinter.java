package ru.mirea.autoservice.console.ui;

import ru.mirea.autoservice.console.model.Car;
import ru.mirea.autoservice.console.model.Client;
import ru.mirea.autoservice.console.model.Mechanic;
import ru.mirea.autoservice.console.model.RepairOrder;
import ru.mirea.autoservice.console.model.TableSnapshot;
import ru.mirea.autoservice.console.util.Formats;

import java.util.List;

public final class TablePrinter {

    private static final int[] ORDER_WIDTHS = {4, 8, 10, 18, 12, 10, 14, 10, 12, 10};
    private static final String[] ORDER_HEADERS =
            {"ID", "Номер", "Приём", "Клиент", "Авто", "Госномер", "Механик", "Статус", "Стоимость", "Выдача"};

    private static final int[] CLIENT_WIDTHS = {4, 28, 14, 24, 5, 8};
    private static final String[] CLIENT_HEADERS = {"ID", "ФИО", "Телефон", "E-mail", "Авто", "Заказов"};

    private static final int[] CAR_WIDTHS = {4, 24, 12, 12, 6, 12};
    private static final String[] CAR_HEADERS = {"ID", "Владелец", "Марка", "Модель", "Год", "Госномер"};

    private static final int[] MECHANIC_WIDTHS = {4, 30, 28};
    private static final String[] MECHANIC_HEADERS = {"ID", "ФИО", "Специализация"};

    private final ConsoleIO io;

    public TablePrinter(ConsoleIO io) {
        this.io = io;
    }

    public void printOrders(List<RepairOrder> orders) {
        if (orders.isEmpty()) {
            io.println("Заказ-наряды не найдены.");
            return;
        }
        printHeader(ORDER_HEADERS, ORDER_WIDTHS);
        for (RepairOrder order : orders) {
            io.println(row(new String[] {
                    String.valueOf(order.id()),
                    order.orderNumber(),
                    Formats.date(order.createdDate()),
                    order.clientName(),
                    order.carTitle(),
                    order.plate(),
                    Formats.shortName(order.mechanicName()),
                    order.status().title(),
                    Formats.money(order.cost()),
                    Formats.date(order.completedDate())
            }, ORDER_WIDTHS));
        }
        io.println("-".repeat(totalWidth(ORDER_WIDTHS)));
        io.println("Всего записей: " + orders.size());
    }

    public void printClients(List<Client> clients) {
        if (clients.isEmpty()) {
            io.println("Клиенты не найдены.");
            return;
        }
        printHeader(CLIENT_HEADERS, CLIENT_WIDTHS);
        for (Client client : clients) {
            io.println(row(new String[] {
                    String.valueOf(client.id()),
                    client.fullName(),
                    client.phone(),
                    Formats.text(client.email()),
                    String.valueOf(client.carCount()),
                    String.valueOf(client.orderCount())
            }, CLIENT_WIDTHS));
        }
        io.println("-".repeat(totalWidth(CLIENT_WIDTHS)));
        io.println("Всего клиентов: " + clients.size());
    }

    public void printCars(List<Car> cars) {
        if (cars.isEmpty()) {
            io.println("Автомобили не найдены.");
            return;
        }
        printHeader(CAR_HEADERS, CAR_WIDTHS);
        for (Car car : cars) {
            io.println(row(new String[] {
                    String.valueOf(car.id()),
                    car.ownerName(),
                    car.brand(),
                    car.model(),
                    String.valueOf(car.year()),
                    car.plate()
            }, CAR_WIDTHS));
        }
        io.println("-".repeat(totalWidth(CAR_WIDTHS)));
        io.println("Всего автомобилей: " + cars.size());
    }

    public void printMechanics(List<Mechanic> mechanics) {
        if (mechanics.isEmpty()) {
            io.println("Механики не найдены.");
            return;
        }
        printHeader(MECHANIC_HEADERS, MECHANIC_WIDTHS);
        for (Mechanic mechanic : mechanics) {
            io.println(row(new String[] {
                    String.valueOf(mechanic.id()),
                    mechanic.fullName(),
                    mechanic.specialization()
            }, MECHANIC_WIDTHS));
        }
        io.println("-".repeat(totalWidth(MECHANIC_WIDTHS)));
        io.println("Всего механиков: " + mechanics.size());
    }

    public void printCarChoices(List<Car> cars) {
        io.println("Автомобили (ID — марка и модель — госномер — владелец):");
        for (Car car : cars) {
            io.println(String.format("  %2d — %-18s — %-10s — %s",
                    car.id(), car.title(), car.plate(), car.ownerName()));
        }
    }

    public void printMechanicChoices(List<Mechanic> mechanics) {
        io.println("Механики (ID — ФИО — специализация):");
        for (Mechanic mechanic : mechanics) {
            io.println(String.format("  %2d — %-28s — %s",
                    mechanic.id(), mechanic.fullName(), mechanic.specialization()));
        }
    }

    public void printOrderCard(RepairOrder order) {
        io.println("=".repeat(78));
        io.println("Заказ-наряд " + order.orderNumber() + " (ID " + order.id() + ")");
        io.println("-".repeat(78));
        io.println("Статус:         " + order.status().title());
        io.println("Дата приёма:    " + Formats.date(order.createdDate()));
        io.println("Дата выдачи:    " + Formats.date(order.completedDate()));
        io.println("Срок ремонта:   " + (order.repairDays().isPresent()
                ? order.repairDays().getAsLong() + " дн." : "—"));
        io.println("Стоимость:      " + Formats.money(order.cost()));
        io.println("Клиент:         " + order.clientName() + ", " + order.clientPhone());
        io.println("Автомобиль:     " + order.carTitle() + ", " + order.carYear() + " г., " + order.plate());
        io.println("Механик:        " + order.mechanicName() + " (" + order.mechanicSpecialization() + ")");
        io.println("Описание работ: " + order.description());
        io.println("=".repeat(78));
    }

    public void printSnapshot(TableSnapshot snapshot) {
        io.println();
        io.println("=== " + snapshot.title() + " ===");
        if (snapshot.rows().isEmpty()) {
            io.println("(таблица пуста)");
            return;
        }
        int columns = snapshot.headers().size();
        int[] widths = new int[columns];
        for (int i = 0; i < columns; i++) {
            int width = snapshot.headers().get(i).length();
            for (List<String> row : snapshot.rows()) {
                width = Math.max(width, row.get(i).length());
            }
            widths[i] = Math.min(Math.max(width, 3), 24);
        }
        printHeader(snapshot.headers().toArray(new String[0]), widths);
        for (List<String> row : snapshot.rows()) {
            io.println(row(row.toArray(new String[0]), widths));
        }
        io.println("Строк: " + snapshot.rows().size());
    }

    private void printHeader(String[] headers, int[] widths) {
        io.println(row(headers, widths));
        io.println("-".repeat(totalWidth(widths)));
    }

    private static String row(String[] values, int[] widths) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < widths.length; i++) {
            if (i > 0) {
                line.append(' ');
            }
            line.append(fit(values[i], widths[i]));
        }
        return line.toString();
    }

    private static String fit(String value, int width) {
        String text = value == null ? "" : value;
        if (text.length() > width) {
            text = width <= 1 ? text.substring(0, width) : text.substring(0, width - 1) + "…";
        }
        return String.format("%-" + width + "s", text);
    }

    private static int totalWidth(int[] widths) {
        int total = 0;
        for (int width : widths) {
            total += width;
        }
        return total + widths.length - 1;
    }
}
