package com.carbounty.helper;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Component
public class MailService {
	
	@Autowired
	private JavaMailSender mailSender;

	public void sendSimpleMail(String toEmail, String subject, String body) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(toEmail);
		message.setText(body);
		message.setSubject(subject);
		mailSender.send(message);
		System.out.println("Mail Sent Successfully...");

	}

	public void sendHtmlMail(String toEmail, String subject, String htmlBody) {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
		try {
			helper.setTo(toEmail);
			helper.setSubject(subject);
			helper.setText(htmlBody, true); // Enable HTML content
			mailSender.send(message);
			System.out.println("Mail Sent Successfully...");
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public int randomOtpForMail() {
		Random random = new Random();
		int otp = 100000 + random.nextInt(899999);
		System.out.println("generated otp is: " + otp);
		return otp;
	}
}
