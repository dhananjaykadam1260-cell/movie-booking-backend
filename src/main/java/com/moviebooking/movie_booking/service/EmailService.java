package com.moviebooking.movie_booking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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
}