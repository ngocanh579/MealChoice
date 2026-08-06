package vn.codegyme.meal_choice.utils;

import java.util.regex.Pattern;

public final class EmailValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }
}
