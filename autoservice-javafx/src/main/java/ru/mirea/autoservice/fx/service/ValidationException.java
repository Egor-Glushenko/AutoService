package ru.mirea.autoservice.fx.service;

import java.util.List;
import java.util.stream.Collectors;

public class ValidationException extends RuntimeException {

    private final List<FieldError> errors;

    public ValidationException(List<FieldError> errors) {
        super(errors.stream().map(FieldError::message).collect(Collectors.joining("; ")));
        this.errors = List.copyOf(errors);
    }

    public List<FieldError> errors() {
        return errors;
    }
}
