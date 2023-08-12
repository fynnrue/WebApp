package com.gpse.basis;

import com.gpse.basis.domain.SesamUser;
import com.gpse.basis.domain.service.CredentialService;
import com.gpse.basis.domain.service.UserService;
import com.gpse.basis.web.UserController;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class SesamUserTest {


    private AutoCloseable closeable;
    @Autowired
    private UserService userService;

    @Autowired
    private CredentialService credentialService;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }


    @Test
    void userCreation() {
        // Test to create a user
        final String forename = "Max";
        final String surname = "Mustermann";
        final String email = "max@mail.com";
        final String password = "passwort";
        final String[] roles = {""};
        userService.createUser(email, password, forename, surname, roles);
        assertThat(userService.emailExists(email)).isTrue();
    }

    @Test
    void userChangePassword() {
        String newPassword = "newPassword";
        userService.createUser("max.mustermann@gmail.com", "passwort", "Max", "Mustermann", "");
        String token = userService.generatePasswordResetToken("max.mustermann@gmail.com");
        boolean successful = userService.resetPassword("max.mustermann@gmail.com", newPassword, token);
        System.out.println(token);
        assertThat(successful).isTrue();
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

}
