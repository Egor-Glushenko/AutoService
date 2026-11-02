package ru.mirea.autoservice.console.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Locale;
import java.util.OptionalInt;

public final class ConsoleIO {

    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, consoleCharset()));
    private final PrintStream out = System.out;

    public void println() {
        out.println();
    }

    public void println(String message) {
        out.println(message);
    }

    public void error(String message) {
        out.println("[!] " + message);
    }

    public String readLine(String prompt) {
        out.print(prompt);
        out.flush();
        try {
            String line = reader.readLine();
            if (line == null) {
                throw new InputClosedException();
            }
            return line.strip();
        } catch (IOException e) {
            throw new InputClosedException();
        }
    }

    public String readRequired(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isEmpty()) {
                return value;
            }
            error("Значение не может быть пустым.");
        }
    }

    public String readLineWithDefault(String prompt, String defaultValue) {
        String suffix = defaultValue == null || defaultValue.isBlank() ? "" : " [" + defaultValue + "]";
        String value = readLine(prompt + suffix + ": ");
        if (!value.isEmpty()) {
            return value;
        }
        return defaultValue == null ? "" : defaultValue;
    }

    public String readRequiredWithDefault(String prompt, String defaultValue) {
        while (true) {
            String value = readLineWithDefault(prompt, defaultValue);
            if (!value.isEmpty()) {
                return value;
            }
            error("Значение не может быть пустым.");
        }
    }

    public OptionalInt readInt(String prompt) {
        while (true) {
            String text = readLine(prompt);
            if (text.isEmpty()) {
                return OptionalInt.empty();
            }
            try {
                return OptionalInt.of(Integer.parseInt(text));
            } catch (NumberFormatException e) {
                error("Ошибка: введите целое число.");
            }
        }
    }

    public int readChoice(String prompt, int min, int max) {
        while (true) {
            OptionalInt value = readInt(prompt);
            if (value.isEmpty()) {
                error("Введите номер пункта меню.");
                continue;
            }
            int choice = value.getAsInt();
            if (choice < min || choice > max) {
                error("Ошибка: допустимы значения от " + min + " до " + max + ".");
                continue;
            }
            return choice;
        }
    }

    public boolean readYesNo(String prompt, boolean defaultValue) {
        while (true) {
            String text = readLine(prompt).toLowerCase(Locale.ROOT);
            if (text.isEmpty()) {
                return defaultValue;
            }
            if (text.equals("y") || text.equals("yes") || text.equals("д") || text.equals("да")) {
                return true;
            }
            if (text.equals("n") || text.equals("no") || text.equals("н") || text.equals("нет")) {
                return false;
            }
            error("Ответьте «y» (да) или «n» (нет).");
        }
    }

    private static Charset consoleCharset() {
        String name = System.getProperty("stdin.encoding");
        if (name == null || name.isBlank()) {
            name = System.getProperty("native.encoding");
        }
        if (name == null || name.isBlank()) {
            return Charset.defaultCharset();
        }
        try {
            return Charset.forName(name);
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            return Charset.defaultCharset();
        }
    }
}
