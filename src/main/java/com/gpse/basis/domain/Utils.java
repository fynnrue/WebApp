package com.gpse.basis.domain;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.regex.Pattern;

public class Utils {

    /**
     * This method checks if the user input is correct,
     * so he can be registered.
     *
     * @param surname  surname of the user
     * @param forename forename of the user
     * @param email    hopefully correct email from user
     * @param password hopefully strong password user has chosen
     * @return if the users input is valid
     */
    public static boolean isValidUserRegistration(final String forename, final String surname, final String email, final String password) {
        // Check if every field is not null:
        if (forename == null || surname == null || email == null || password == null) {
            return false;
        } else if (surname.isEmpty() || forename.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return false;
        }

        // Check if the email is valid:
        final String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        final Pattern pattern = Pattern.compile(emailPattern);

        return pattern.matcher(email).matches();
    }

    /**
     * Returns HttpHeaders object with required headers for issuing API requests.
     *
     * @param username the username used for authentication
     * @param password the password used for authentication
     * @return HttpHeaders object with API headers
     */
    public static HttpHeaders issueAPIHeaders(String username, String password) {
        String credentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Basic " + encodedCredentials);
        return headers;
    }

}
