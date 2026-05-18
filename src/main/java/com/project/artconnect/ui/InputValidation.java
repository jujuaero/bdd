package com.project.artconnect.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.css.PseudoClass;
import java.util.regex.Pattern;

/**
 * Live input validation for dialog forms — feedback appears while typing.
 */
public final class InputValidation {

    private static final PseudoClass VALID = PseudoClass.getPseudoClass("field-valid");
    private static final PseudoClass INVALID = PseudoClass.getPseudoClass("field-invalid");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private InputValidation() {}

    public enum Type {
        /** Required whole number. */
        INTEGER,
        /** Required positive whole number (> 0). */
        POSITIVE_INTEGER,
        /** Optional whole number — empty is valid. */
        OPTIONAL_INTEGER,
        /** Required decimal number. */
        DOUBLE,
        /** Required positive decimal (> 0). */
        POSITIVE_DOUBLE,
        /** Optional decimal — empty is valid. */
        OPTIONAL_DOUBLE,
        /** Required email address. */
        EMAIL,
        /** Optional email — empty is valid, otherwise must be a valid address. */
        OPTIONAL_EMAIL
    }

    public static final class ValidatedField {
        private final BooleanProperty valid = new SimpleBooleanProperty(true);
        private final Label hint = new Label();

        ValidatedField() {
            hint.getStyleClass().add("validation-hint");
            hint.setVisible(false);
            hint.setManaged(false);
        }

        public BooleanProperty validProperty() {
            return valid;
        }

        public Label hintLabel() {
            return hint;
        }
    }

    /**
     * Attaches live validation to a text field. Returns a handle whose {@code validProperty()}
     * tracks whether the current value is acceptable.
     */
    public static ValidatedField attach(TextField field, Type type, String fieldLabel) {
        ValidatedField vf = new ValidatedField();
        Runnable validate = () -> runValidation(field, vf, type, fieldLabel);
        field.textProperty().addListener((obs, oldVal, newVal) -> validate.run());
        validate.run();
        return vf;
    }

    /** Adds a label + field + hint row to a dialog grid. */
    public static void addRow(GridPane grid, int row, String labelText, TextField field, ValidatedField validated) {
        grid.add(new Label(labelText), 0, row);
        VBox fieldBox = new VBox(2, field, validated.hintLabel());
        grid.add(fieldBox, 1, row);
        GridPane.setHgrow(fieldBox, Priority.ALWAYS);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    /** Disables the OK button while any validated field is invalid. */
    public static void bindOkButton(Dialog<?> dialog, ValidatedField... fields) {
        Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        if (ok == null) return;
        BooleanBinding allValid = Bindings.createBooleanBinding(
                () -> {
                    for (ValidatedField f : fields) {
                        if (!f.validProperty().get()) return false;
                    }
                    return true;
                },
                extractProperties(fields));
        ok.disableProperty().bind(allValid.not());
    }

    private static BooleanProperty[] extractProperties(ValidatedField[] fields) {
        BooleanProperty[] props = new BooleanProperty[fields.length];
        for (int i = 0; i < fields.length; i++) {
            props[i] = fields[i].validProperty();
        }
        return props;
    }

    private static void runValidation(TextField field, ValidatedField vf, Type type, String fieldLabel) {
        String raw = field.getText() == null ? "" : field.getText().trim();
        String message = validate(raw, type, fieldLabel);
        boolean isValid = message == null;

        vf.valid.set(isValid);
        field.pseudoClassStateChanged(VALID, isValid && !raw.isEmpty());
        field.pseudoClassStateChanged(INVALID, !isValid);

        if (message != null) {
            vf.hint.setText(message);
            vf.hint.getStyleClass().remove("valid");
            vf.hint.setVisible(true);
            vf.hint.setManaged(true);
        } else if (!raw.isEmpty()) {
            vf.hint.setText("✓");
            if (!vf.hint.getStyleClass().contains("valid")) {
                vf.hint.getStyleClass().add("valid");
            }
            vf.hint.setVisible(true);
            vf.hint.setManaged(true);
        } else {
            vf.hint.setVisible(false);
            vf.hint.setManaged(false);
        }
    }

    private static String validate(String raw, Type type, String fieldLabel) {
        String name = fieldLabel == null || fieldLabel.isBlank() ? "This field" : fieldLabel;

        return switch (type) {
            case OPTIONAL_INTEGER -> {
                if (raw.isEmpty()) yield null;
                yield parseInteger(raw, name, false);
            }
            case INTEGER -> {
                if (raw.isEmpty()) yield name + " is required.";
                yield parseInteger(raw, name, false);
            }
            case POSITIVE_INTEGER -> {
                if (raw.isEmpty()) yield name + " is required.";
                String err = parseInteger(raw, name, false);
                if (err != null) yield err;
                if (Integer.parseInt(raw) <= 0) yield name + " must be greater than 0.";
                yield null;
            }
            case OPTIONAL_DOUBLE -> {
                if (raw.isEmpty()) yield null;
                yield parseDouble(raw, name, false);
            }
            case DOUBLE -> {
                if (raw.isEmpty()) yield name + " is required.";
                yield parseDouble(raw, name, false);
            }
            case POSITIVE_DOUBLE -> {
                if (raw.isEmpty()) yield name + " is required.";
                String err = parseDouble(raw, name, false);
                if (err != null) yield err;
                if (Double.parseDouble(raw) <= 0) yield name + " must be greater than 0.";
                yield null;
            }
            case EMAIL -> {
                if (raw.isEmpty()) yield name + " is required.";
                yield parseEmail(raw, name);
            }
            case OPTIONAL_EMAIL -> {
                if (raw.isEmpty()) yield null;
                yield parseEmail(raw, name);
            }
        };
    }

    private static String parseEmail(String raw, String name) {
        if (EMAIL_PATTERN.matcher(raw).matches()) {
            return null;
        }
        return name + " must be a valid email (e.g. name@example.com).";
    }

    private static String parseInteger(String raw, String name, boolean positiveOnly) {
        try {
            Integer.parseInt(raw);
            if (positiveOnly && Integer.parseInt(raw) <= 0) {
                return name + " must be a positive whole number.";
            }
            return null;
        } catch (NumberFormatException e) {
            return name + " must be a whole number (e.g. 42).";
        }
    }

    private static String parseDouble(String raw, String name, boolean positiveOnly) {
        try {
            double v = Double.parseDouble(raw);
            if (positiveOnly && v <= 0) {
                return name + " must be a positive number.";
            }
            return null;
        } catch (NumberFormatException e) {
            return name + " must be a number (e.g. 19.99).";
        }
    }

    /** Parses a validated integer field, or returns null if empty (for optional fields). */
    public static Integer parseOptionalInt(TextField field) {
        String raw = field.getText() == null ? "" : field.getText().trim();
        if (raw.isEmpty()) return null;
        return Integer.parseInt(raw);
    }

    public static int parseRequiredInt(TextField field) {
        return Integer.parseInt(field.getText().trim());
    }

    public static double parseRequiredDouble(TextField field) {
        return Double.parseDouble(field.getText().trim());
    }

    public static String parseRequiredEmail(TextField field) {
        return field.getText().trim();
    }

    public static String parseOptionalEmail(TextField field) {
        String raw = field.getText() == null ? "" : field.getText().trim();
        return raw.isEmpty() ? "" : raw;
    }
}
