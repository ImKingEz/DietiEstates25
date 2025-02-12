package com.dietiestates25ui.handler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormValidator {


    private static final String EMAIL_REGEX = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
    private static final String TELEFONO_REGEX = "^\\d{10}$";
    private static final String PARTITA_IVA_REGEX = "^\\d{11}$";


    private static final Pattern emailPattern = Pattern.compile(EMAIL_REGEX);
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
        Matcher matcher = emailPattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isValidTelefono(String telefono) {
        Matcher matcher = telefonoPattern.matcher(telefono);
        return matcher.matches();
    }

    public static boolean isValidPartitaIVA(String partitaIVA) {
        Matcher matcher = partitaIVAPattern.matcher(partitaIVA);
        return matcher.matches();
    }
}