package com.gpse.basis.web;

import com.gpse.basis.domain.SesamUser;
import com.gpse.basis.domain.Utils;
import com.gpse.basis.domain.credentials.Credential;
import com.gpse.basis.domain.service.CredentialService;
import com.gpse.basis.domain.service.EmailService;
import com.gpse.basis.domain.service.UserService;
import com.gpse.basis.web.exceptions.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


/**
 * The UserController class is a REST controller that handles user-related operations.
 * Exposes various API endpoints for managing users, such as retrieving user information, managing roles and credentials,
 * and performing actions like registration, password reset, and user deletion.
 */

@RestController
@CrossOrigin
@RequestMapping("/api")
public class UserController {


    private final UserService userService;
    private final CredentialService credentialService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(
            final UserService userService,
            final PasswordEncoder passwordEncoder,
            final EmailService emailService,
            final CredentialService credentialService
            ) {
        this.userService = userService;
        this.credentialService = credentialService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * This method is called to retrieve the list of all users.
     *
     * @return the list of all users
     */
    @GetMapping("/users")
    public List<SesamUser> showUsers() {
        log.debug("Request to show all users");
        return userService.showUsers();
    }

    /**
     * This method is called to retrieve a list of unregistered users.
     *
     * @return a list of unregistered users
     */
    @GetMapping("/users/unregistered")
    public List<SesamUser> showUnregisteredUsers() {
        log.debug("Request to show all unregistered users");
        return userService.showUnregisteredUsers();
    }


    /**
     * This method is called to retrieve a user by their email.
     *
     * @param email the email of the user
     * @return the user object with all its details
     */
    @GetMapping("/users/email/{email}")
    @Secured("ROLE_ADMIN")
    public SesamUser getUser(@PathVariable("email") final String email) {
        log.debug("Request to show user with email: {}", email);
        SesamUser user = (SesamUser) userService.loadUserByUsername(email);
        if (user != null) {
            log.info("User with email {} found", email);
            return user;
        } else {
            log.warn("User with email {} not found", email);
            throw new BadRequestException();
        }
    }

    /**
     * This method is called to show the roles of a user.
     *
     * @param email the email of the user
     * @return a list of roles associated with the user
     */
    @GetMapping("/users/{email}/roles")
    @Secured("ROLE_ADMIN")
    public List<String> showUserRoles(@PathVariable("email") final String email) {
        log.debug("Request to show roles of user with email: {}", email);
        SesamUser user = (SesamUser) userService.loadUserByUsername(email);
        if (user != null) {
            log.info("Roles of user with email {} found", email);
            return user.getRoles();
        } else {
            log.warn("Roles of user with email {} not found", email);
            throw new BadRequestException();
        }
    }

    /**
     * This method is called to show the credentials of a user.
     *
     * @param email the email of the user
     * @return an array containing the credentials of the user
     * @throws BadRequestException if the user does not exist
     */
    @GetMapping("/users/{email}/credentials")
    public Object[] showUserCredentials(@PathVariable("email") final String email) {
        log.debug("Request to show credentials of user with email: {}", email);
        SesamUser user = (SesamUser) userService.loadUserByUsername(email);
        if (user != null) {
            log.info("Credentials of user with email {} found", email);
            return user.getIssuableCredentials().toArray();
        } else {
            log.warn("Credentials of user with email {} not found", email);
            throw new BadRequestException();
        }
    }

    /**
     * This method is called by the user to their user.
     *
     * @param request the WebRequest object containing the email and password parameters
     * @return "valid" if the user is deleted successfully, "invalid" otherwise
     */
    @PostMapping("/user/deleteUser")
    public String deleteUser(final WebRequest request) {
        log.debug("Request to delete user");
        final String email = request.getParameter("email");
        final String password = request.getParameter("password");
        log.info("Deleting user with email {}", email);

        if (userService.getUserByEmail(email).isPresent()) {
            if (passwordEncoder.matches(password, userService.getUserByEmail(email).get().getPassword())) {
                userService.deleteUsers(email);
                log.info("User with email {} deleted", email);
                return "valid";
            } else {
                log.warn("User with email {} not deleted", email);
                return "invalid";
            }
        } else {
            log.warn("User with email {} not found/deleted", email);
            return "invalid";
        }
    }

    /**
     * This method is called by the user to change their data.
     *
     * @param request the WebRequest object containing the request parameters
     * @return a string indicating the success of the data change
     */
    @PostMapping("/user/changeUser")
    public String changeData(final WebRequest request) {
        log.debug("Request to change user data");
        final String username = request.getParameter("username");
        final String newForename = request.getParameter("newForename");
        final String newSurname = request.getParameter("newSurname");
        final String newEmail = request.getParameter("newEmail");

        log.info("Changing data of user with email {}", username);
        if (userService.getUserByEmail(username).isPresent()) {
            SesamUser sesamUser = userService.getUserByEmail(username).get();
            userService.deleteUsers(username);
            userService.createUser(newEmail, sesamUser.getPassword(), newForename, newSurname, sesamUser.getRoles().toArray(new String[0]));
            log.info("Data of user with email {} changed", username);
            return "valid";
        } else {
            log.warn("Data of user with email {} not changed", username);
            return "invalid";
        }
    }

    /**
     * This method is called by the user to change their password.
     *
     * @param request the WebRequest object containing the email, oldPassword, and newPassword parameters
     * @return a string indicating the result of the password change (valid or invalid)
     */
    @PostMapping("/user/changePassword")
    public String changePassword(final WebRequest request) {
        log.debug("Request to change user password");
        final String email = request.getParameter("email");
        final String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        log.debug("Changing password of user with email {}", email);
        Optional<SesamUser> user = userService.getUserByEmail(email);

        if (user.isPresent()){
            SesamUser sesamUser = user.get();
            newPassword = passwordEncoder.encode(newPassword);
            log.debug("User trying to change password: {}", sesamUser.getUsername());
            if (passwordEncoder.matches(oldPassword, sesamUser.getPassword())){
                sesamUser.setPassword(newPassword);
                userService.updateUser(sesamUser);
                log.debug("Password of user with email {} changed", email);
                return "valid";
            }
            else {
                log.warn("Password of user with email {} not changed", email);
                return "invalid";
            }
        }
        log.debug("User is not present");
        return "invalid";
    }

    /**
     * Just registers a user.
     *
     * @param request the request that wants to register a user
     * @return a string indicating the registration status ("registered" if successful, "user exists" if user already exists, "invalid data" if the registration data is invalid)
     */
    @PostMapping("/user/registration")
    public String registerUser(final WebRequest request) {
        log.debug("Request to register user");
        // Get username and email out of request:
        final String forename = request.getParameter("forename");
        final String surname = request.getParameter("surname");
        final String email = request.getParameter("email");
        String password = request.getParameter("password");

        log.debug("Registering user with email {}", email);
        if (Utils.isValidUserRegistration(forename, surname, email, password)) {

            // Get plain password and hash it using the password encoder:
            password = passwordEncoder.encode(password);

            String token = this.userService.createUnregisteredUser(email, password, forename, surname, "");
            if (token == null || token.isEmpty()) {
                return "user exists";
            }
            this.emailService.sendRegistrationEmail(email, token);
            log.info("User with email {} registering", email);
            return "registered";
        } else {
            log.warn("User with email {} not registered", email);
            return "invalid data";
        }
    }

    /**
     * This method is called to confirm the registration of a user.
     *
     * @param request The request that wants to confirm the registration of a user.
     *                It should contain the email and token parameters.
     * @return true if the registration is confirmed successfully, false otherwise.
     */
    @PostMapping("/user/registration/confirm")
    public boolean confirmRegistration(final WebRequest request) {
        final String email = request.getParameter("email");
        final String token = request.getParameter("token");

        log.debug("Confirming registration of user with email {}", email);

        return this.userService.registerUser(email, token);
    }

    /**
     * This method is called to retrieve the profile of a user.
     *
     * @param sesamUser the authenticated SesamUser object representing the user
     * @return the profile of the user
     */
    @GetMapping("/profile")
    public SesamUser profileUser(@AuthenticationPrincipal final SesamUser sesamUser) {
        log.debug("Request to get profile of user with email {}", sesamUser.getUsername());
        return sesamUser;
    }

    /**
     * Method is called when an admin tries to filter the list of all users in the database.
     *
     * @param request the HTTP request containing the filter information
     * @return a list of all users that fit the specified filter restrictions. If no matching users are found, a BadRequestException is thrown.
     */
    @GetMapping("/admin/users/filter")
    @Secured("ROLE_ADMIN")
    public List<SesamUser> filterUsers(final WebRequest request) {
        log.debug("Request to filter users");
        // Get filter information from request
        final String permission = request.getParameter("permission");
        final String activated = request.getParameter("activated");
        final String searchType = request.getParameter("searchType");
        final String search = request.getParameter("search");

        return userService.getUsersFiltered(permission, activated, searchType, search);
    }

    /**
     * Method is called when admin tries to activate a list of users.
     *
     * @param request http request with email addresses of users to be activated.
     * @return "valid" if activation is successful, "invalid" otherwise.
     */
    @PostMapping("/admin/users/activate")
    @Secured("ROLE_ADMIN")
    public String activateUsers(final WebRequest request) {
        log.debug("Request to activate users");
        // Get emails information from request
        final String emails = request.getParameter("emails");

        boolean status = userService.activateUsers(emails);

        if (status) {
            log.info("Users activated");
            return "valid";
        } else {
            log.warn("Users not activated");
            return "invalid";
        }
    }

    /**
     * Method is called when admin tries to deactivate users by providing their email addresses.
     *
     * @param request http request with emails of users to be deactivated.
     * @return "valid" if deactivation was successful, "invalid" otherwise.
     */
    @PostMapping("/admin/users/deactivate")
    @Secured("ROLE_ADMIN")
    public String deactivateUsers(final WebRequest request) {
        log.debug("Request to deactivate users");
        // Get emails information from request
        final String emails = request.getParameter("emails");

        boolean status = userService.deactivateUsers(emails);

        if (status) {
            log.info("Users deactivated");
            return "valid";
        } else {
            log.warn("Users not deactivated");
            return "invalid";
        }
    }

    /**
     * Method is called when admin tries to delete multiple users from the database.
     *
     * @param request HTTP request with email information of users to be deleted.
     * @return A string indicating the result of the deletion. Possible values are "valid" if the deletion is successful,
     * or "invalid" if the deletion fails.
     */
    @PostMapping("/admin/users/delete")
    @Secured("ROLE_ADMIN")
    public String deleteUsers(final WebRequest request) {
        log.debug("Request to delete users");
        // Get emails information from request
        final String emails = request.getParameter("emails");

        boolean status = userService.deleteUsers(emails);

        if (status) {
            log.info("Users deleted");
            return "valid";
        } else {
            log.warn("Users not deleted");
            return "invalid";
        }
    }

    /**
     * Method is called when a user requests to reset their password via email.
     *
     * @param email the email of the user who wants to reset their password.
     * @return a ResponseEntity with a success message if the email is sent successfully, or a ResponseEntity with
     *         a "User not found" message if the user does not exist.
     */
    @PostMapping("/user/requestResetPasswordPerMail")
    public ResponseEntity<String> requestResetPasswordPerMail(@RequestParam("email") String email) {
        log.debug("Request to reset password for user with email {}", email);
        String token = userService.generatePasswordResetToken(email);
        if (token == null) {
            log.warn("User with email {} not found", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        emailService.sendPasswordResetEmail(email, token);
        log.info("Confirmation email sent successfully to user with email {}", email);
        return ResponseEntity.ok("Email sent successfully");
    }


    /**
     * Method to perform a password reset for a user.
     *
     * @param email    The email of the user requesting the password reset.
     * @param password The new password to set for the user.
     * @param token    The token associated with the password reset request.
     * @return ResponseEntity with a success message if the password reset is successful,
     *         or a bad request message if it fails.
     */
    @PostMapping("/user/resetPassword")
    public ResponseEntity<String> resetPassword(@RequestParam String email,
                                                @RequestParam String password,
                                                @RequestParam String token) {
        log.debug("Request to reset password for user with email {}", email);
        password = passwordEncoder.encode(password);
        if (userService.resetPassword(email, password, token)) {
            log.info("Password reset successfully for user with email {}", email);
            return ResponseEntity.ok("Email changed successfully");
        }
        log.warn("Password could not be changed for user with email {}", email);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email could not be changed");
    }

    /**
     * This method is called to set the credentials an issuer can issue.
     *
     * @param request the request that wants to set the credentials of an issuer
     */
    @PostMapping("/admin/users/credentials/allowIssue")
    @Secured("ROLE_ADMIN")
    public void setIssuableCredentials(final WebRequest request) {
        log.debug("Request to set issueable credentials of an issuer");
        final String email = request.getParameter("email");
        final String credentials = request.getParameter("credentials");

        Collection<Credential> credsList = new ArrayList<>();

        if (credentials == null) {
            throw new BadRequestException();
        }

        if (!credentials.isEmpty()) {
            for (String cred : credentials.split(",")) {
                Optional<Credential> credential = credentialService.getCredentialById(Long.parseLong(cred));
                credential.ifPresent(credsList::add);
            }

            SesamUser user = (SesamUser) userService.loadUserByUsername(email);
            if (user != null) {
                user.setIssuableCredentials(credsList);
                userService.updateUser(user);
                log.info("Issueable credentials of an issuer set successfully {}, {}", email, credsList);
                return;
            }
        } else {
            SesamUser user = (SesamUser) userService.loadUserByUsername(email);
            if (user != null) {
                user.setIssuableCredentials(credsList);
                userService.updateUser(user);
                log.info("Issueable credentials of an issuer set successfully {}, {}", email, credsList);
                return;
            }
        }
        log.warn("Could not set issueable credentials of an issuer");
        throw new BadRequestException();
    }

    /**
     * This method is called to show the credentials that an issuer can issue for a specific user.
     *
     * @param email the email of the user for whom the credentials should be shown
     * @return the list of credentials that the issuer can issue for the user
     */
    @GetMapping("admin/users/credentials/issuable/{email}")
    @Secured({"ROLE_ADMIN", "ROLE_ISSUER"})
    public List<Credential> showIssuableCredentials(@PathVariable("email") final String email) {
        log.debug("Request to show issueable credentials of an issuer");
        SesamUser user = (SesamUser) userService.loadUserByUsername(email);

        if (user != null) {
            return user.getIssuableCredentials().stream().toList();
        } else {
            throw new BadRequestException();
        }
    }

    /**
     * This method is called to set the roles of a user.
     *
     * @param email   the email of the user
     * @param request the request that wants to set the roles of a user
     * @throws BadRequestException if the roles parameter is null or empty, or if the user cannot be found
     */
    @PostMapping("/admin/users/roles/{email}")
    @Secured("ROLE_ADMIN")
    public void setRoles(@PathVariable("email") final String email, final WebRequest request) {
        log.debug("Request to set roles of a user");
        final String roles = request.getParameter("roles");

        List<String> rolesList = new ArrayList<>();

        if (roles == null) {
            throw new BadRequestException();
        }

        SesamUser user = (SesamUser) userService.loadUserByUsername(email);
        if (!roles.isEmpty()) {
            if (user != null) {
                String[] rolesArray = roles.split(",");
                for (String role : rolesArray) {
                    switch (role) {
                        case "1" -> rolesList.add(SesamUser.ROLE_ADMIN);
                        case "2" -> rolesList.add(SesamUser.ROLE_ISSUER);
                        case "3" -> rolesList.add(SesamUser.ROLE_EDITOR);
                    }
                }

                user.setRoles(rolesList);
                userService.updateUser(user);
                log.info("Roles of a user set successfully {}, {}", email, rolesList);
                return;
            }
        } else {
            if (user != null) {
                user.setRoles(rolesList);
                userService.updateUser(user);
                log.info("Roles of a user set successfully {}, {}", email, rolesList);
                return;
            }
        }
        log.warn("Could not set roles of a user");
        throw new BadRequestException();
    }
}
