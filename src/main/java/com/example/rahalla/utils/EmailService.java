package com.example.rahalla.utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class EmailService {
    private static final String CONFIG_FILE = "email.properties";
    private static Properties emailProps;
    private static Session session;
    private static ExecutorService emailExecutor;
    private static volatile boolean initialized = false;

    static {
        initializeService();
    }

    private static synchronized void initializeService() {
        if (!initialized) {
            try {
                loadConfiguration();
                createSession();
                emailExecutor = Executors.newSingleThreadExecutor();
                initialized = true;
            } catch (IOException e) {
                System.out.println("Failed to initialize email service" + e.getMessage());
                System.out.println("Email service initialization failed" + e.getMessage());
            }
        }
    }

    private static void loadConfiguration() throws IOException {
        emailProps = new Properties();
        try (InputStream input = EmailService.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                System.out.println("Unable to find " + CONFIG_FILE);
            }
            emailProps.load(input);
        }

        validateConfiguration();
    }

    private static void validateConfiguration() throws IOException {
        String[] requiredProps = {"mail.smtp.host", "mail.smtp.port", "mail.username", "mail.password"};
        for (String prop : requiredProps) {
            if (!emailProps.containsKey(prop)) {
                System.out.println("Missing required property: " + prop);
            }
        }
    }

    private static void createSession() {
        session = Session.getInstance(emailProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        emailProps.getProperty("mail.username"),
                        emailProps.getProperty("mail.password")
                );
            }
        });
    }

    public static void sendEmail(String to, String subject, String content) throws MessagingException {
        validateEmailParameters(to, subject, content);

        emailExecutor.submit(() -> {
            try {
                MimeMessage message = createMessage(to, subject, content);
                Transport.send(message);
                System.out.println("Email sent successfully to: " + to);
            } catch (Exception e) {
                System.out.println("Failed to send email to: " + to + e.getMessage());
                System.out.println("Failed to send email" + e.getMessage());
            }
        });
    }

    private static void validateEmailParameters(String to, String subject, String content) {
        if (to == null || to.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient email cannot be null or empty");
        }
        if (!to.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email address format: " + to);
        }
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject cannot be null or empty");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
    }

    private static MimeMessage createMessage(String to, String subject, String content) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailProps.getProperty("mail.username")));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(content);
        return message;
    }

    public static void shutdown() {
        if (emailExecutor != null && !emailExecutor.isShutdown()) {
            emailExecutor.shutdown();
            try {
                if (!emailExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    emailExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                emailExecutor.shutdownNow();
            }
        }
    }
}