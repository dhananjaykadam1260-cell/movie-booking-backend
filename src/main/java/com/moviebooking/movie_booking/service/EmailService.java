package com.moviebooking.movie_booking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(String email, String name) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("Welcome to Movie Booking");

        message.setText(
                "Hello " + name + ",\n\n" +
                "Welcome to our Movie Booking platform.\n\n" +
                "Your registration was successful.\n" +
                "You can now browse movies and book tickets.\n\n" +
                "Thank you!"
        );

        mailSender.send(message);
    }
    
    public void sendTicketEmail(
            String email,
            String name,
            byte[] pdf,
            Long bookingId) {

        try {

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(email);

            helper.setSubject(
                    "Your Movie Ticket - Booking #" + bookingId
            );

            helper.setText(
                    "Hello " + name + ",\n\n" +
                    "Your movie booking is confirmed.\n" +
                    "Your ticket is attached as a PDF.\n\n" +
                    "Enjoy your movie!"
            );

            helper.addAttachment(
                    "Movie-Ticket-" + bookingId + ".pdf",
                    new ByteArrayResource(pdf)
            );

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to send ticket email", e
            );
        }
    }
}