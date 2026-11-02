package ru.mirea.autoservice.fx.ui.util;

import javafx.scene.Node;

public final class FieldMarks {

    private static final String ERROR_CLASS = "field-error";

    private FieldMarks() {
    }

    public static void set(Node node, boolean error) {
        node.getStyleClass().removeAll(ERROR_CLASS);
        if (error) {
            node.getStyleClass().add(ERROR_CLASS);
        }
    }
}
