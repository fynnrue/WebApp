package com.gpse.basis.domain;

import java.util.regex.Pattern;

public class RegistrationHelper {

    /**
     * This method checks if the user input is correct
     * so he can be registed.
     *
     * @param email    hopefully correct email from user
     * @param password hopefully strong password user has chosen
     * @return if the users input is valid
     */
    public static boolean isValidUserRegistration(
            final String email,
            final String password
    ) {
        // Check if every field is not null:
        if (email == null || password == null) {
            return false;
        }

        // Check if the email is valid:
        final String emailPattern =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        final Pattern pattern = Pattern.compile(emailPattern);
        final boolean isEmail = pattern.matcher(email).matches();

        return isEmail;
    }

}
