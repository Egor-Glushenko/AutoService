package ru.mirea.autoservice.console.exception;

import java.util.List;
import java.util.stream.Collectors;

public class ValidationException extends BusinessException {

    private final List<FieldError> errors;

    public ValidationException(List<FieldError> errors) {
        super(errors.stream().map(FieldError::message).collect(Collectors.joining("; ")));
        this.errors = List.copyOf(errors);
    }

    public List<FieldError> errors() {
        return errors;
    }
}
