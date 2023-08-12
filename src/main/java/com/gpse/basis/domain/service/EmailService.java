package com.gpse.basis.domain.service;

import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Async;

public interface EmailService {
    void sendEmail(String to, String subject, String text) throws MailException;

    @Async
    void sendPasswordResetEmail(String to, String token) throws MailException;

    @Async
    void sendRegistrationEmail(String to, String token) throws MailException;
}
