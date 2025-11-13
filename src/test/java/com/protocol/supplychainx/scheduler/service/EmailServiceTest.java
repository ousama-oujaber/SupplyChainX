package com.protocol.supplychainx.scheduler.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private final String fromEmail = "test@supplychainx.com";
    private final String toEmail = "recipient@example.com";
    private final String subject = "Test Subject";
    private final String body = "Test Body";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", fromEmail);
    }

    @Test
    @DisplayName("Should send simple email successfully")
    void testSendSimpleEmail_Success() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendSimpleEmail(toEmail, subject, body);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when simple email fails")
    void testSendSimpleEmail_Failure() {
        // Arrange
        doThrow(new RuntimeException("Mail server error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            emailService.sendSimpleEmail(toEmail, subject, body)
        );
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should send HTML email successfully")
    void testSendHtmlEmail_Success() {
        // Arrange
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        String htmlBody = "<html><body><h1>Test</h1></body></html>";

        // Act
        emailService.sendHtmlEmail(toEmail, subject, htmlBody);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when HTML email fails")
    void testSendHtmlEmail_Failure() {
        // Arrange
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Mail server error"))
                .when(mailSender).send(any(MimeMessage.class));

        String htmlBody = "<html><body><h1>Test</h1></body></html>";

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            emailService.sendHtmlEmail(toEmail, subject, htmlBody)
        );
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should send HTML email to multiple recipients successfully")
    void testSendHtmlEmailToMultiple_Success() {
        // Arrange
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        String[] recipients = {"user1@example.com", "user2@example.com"};
        String htmlBody = "<html><body><h1>Test</h1></body></html>";

        // Act
        emailService.sendHtmlEmailToMultiple(recipients, subject, htmlBody);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when sending to multiple recipients fails")
    void testSendHtmlEmailToMultiple_Failure() {
        // Arrange
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Mail server error"))
                .when(mailSender).send(any(MimeMessage.class));

        String[] recipients = {"user1@example.com", "user2@example.com"};
        String htmlBody = "<html><body><h1>Test</h1></body></html>";

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            emailService.sendHtmlEmailToMultiple(recipients, subject, htmlBody)
        );
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}
