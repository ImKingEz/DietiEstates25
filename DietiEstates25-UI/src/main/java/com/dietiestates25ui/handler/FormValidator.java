package com.dietiestates25ui.handler;

public class FormValidator {

    public static final int DOMAIN_MIN_LENGTH = 2;

    private FormValidator() {
    }

    public static boolean isValidEmail(String email) {
        if (email.isBlank()) {
            return false;
        }
        boolean hasAt = false;
        for (int i = 0; i < email.length(); i++) {
            if (hasAt) {
                return isEmailValidAfterAt(email, i);
            } else if (email.charAt(i) == '@' && i > 0) {
                hasAt = true;
            } else {
                if (!isEmailValidBeforeAt(email, i)) {
                    return false;
                }
            }
        }
        return false;
    }

    private static boolean isEmailValidBeforeAt(String email, int i) {
        return Character.isLetter(email.charAt(i)) || Character.isDigit(email.charAt(i)) || email.charAt(i) == '.' || email.charAt(i) == '-' || email.charAt(i) == '_';
    }

    private static boolean isEmailValidAfterAt(String email, int i) {
        boolean hasDot = false;
        for (int j = i; j < email.length(); j++) {
            if (j == i && !Character.isLetter(email.charAt(j))) {
                return false;
            } else if (!Character.isLetter(email.charAt(j))) {
                if (email.charAt(j) == '.' && !hasDot && j < email.length() - DOMAIN_MIN_LENGTH) {
                    hasDot = true;
                } else {
                    return false;
                }
            }
        }
        return hasDot;
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
}