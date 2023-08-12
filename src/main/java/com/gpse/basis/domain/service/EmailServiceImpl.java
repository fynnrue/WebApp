package com.gpse.basis.domain.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;


@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;
    private final Environment environment;

    @Autowired
    public EmailServiceImpl(JavaMailSender emailSender, Environment environment) {
        this.emailSender = emailSender;
        this.environment = environment;
    }

    @Override
    @Async
    public void sendEmail(String to, String subject, String text) throws MailException {
        final var from = environment.getProperty("spring.mail.username");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String token) throws MailException {
        final var from = environment.getProperty("spring.mail.username");

        try (InputStream fileStream = getClass().getResourceAsStream("/templates/resetPasswordMail.html")) {
            MimeMessage message = emailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            assert fileStream != null;
            String content = new String(fileStream.readAllBytes());
            content = content.replace("{token}", token);
            content = content.replace("{email}", to);

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Password reset");
            helper.setText(content, true);
            emailSender.send(message);
        } catch (MessagingException | IOException e) {
            System.out.println("Error while sending email...");
        }
    }

    @Override
    @Async
    public void sendRegistrationEmail(String to, String token) throws MailException {
        final var from = environment.getProperty("spring.mail.username");

        try (InputStream fileStream = getClass().getResourceAsStream("/templates/registrationMail.html")) {
            MimeMessage message = emailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            assert fileStream != null;
            String content = new String(fileStream.readAllBytes());
            content = content.replace("{token}", token);
            content = content.replace("{email}", to);

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Registration");
            helper.setText(content, true);
            emailSender.send(message);
        } catch (MessagingException | IOException e) {
            System.out.println("Error while sending email...");
        }
    }
}
