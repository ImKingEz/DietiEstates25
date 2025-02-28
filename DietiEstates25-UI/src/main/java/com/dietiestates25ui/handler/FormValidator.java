package com.dietiestates25ui.handler;

import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import org.apache.commons.validator.routines.EmailValidator;

public class FormValidator {

    private static final EmailValidator validator = EmailValidator.getInstance();

    private static final String TELEFONO_REGEX = "^\\d{10}$";
    private static final String PARTITA_IVA_REGEX = "^\\d{11}$";

    private static final Pattern telefonoPattern = Pattern.compile(TELEFONO_REGEX);
    private static final Pattern partitaIVAPattern = Pattern.compile(PARTITA_IVA_REGEX);

    private FormValidator() {
    }

    public static boolean isValidPassword(String password) {
        if (password.length() < 8) {
            return false;
        }
        boolean hasDigit = false;
        boolean hasMaiusc = false;
        for (int i = 0; i < password.length(); i++) {
            if (Character.isDigit(password.charAt(i)) && !hasDigit) {
                hasDigit = true;
            }
            if (Character.isUpperCase(password.charAt(i)) && !hasMaiusc) {
                hasMaiusc = true;
            }
        }
        return hasDigit && hasMaiusc;
    }

    public static boolean isValidEmail(String email) {
        return validator.isValid(email);
    }

    public static boolean isValidTelefono(String telefono) {
        Matcher matcher = telefonoPattern.matcher(telefono);
        return matcher.matches();
    }

    public static boolean isValidPartitaIVA(String partitaIVA) {
        Matcher matcher = partitaIVAPattern.matcher(partitaIVA);
        return matcher.matches();
    }

    public static void setupTextFormatter(TextField textField) {
        UnaryOperator<TextFormatter.Change> numberFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*(\\.\\d*)?")) {
                return change;
            }
            return null;
        };
        TextFormatter<Object> textFormatter = new TextFormatter<>(numberFilter);
        textField.setTextFormatter(textFormatter);
    }
}