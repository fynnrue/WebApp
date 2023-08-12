package com.gpse.basis;
import com.gpse.basis.domain.service.EmailService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailTest {
    @Autowired
    private EmailService emailService;

    @Test
    void sendEmail() {
        // Test to send an email
        final String to = "lula.pouros74@ethereal.email";
        final String subject = "Test";
        final String text = "This is a test";
        try {
        emailService.sendEmail(to, subject, text);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
