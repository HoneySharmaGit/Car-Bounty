package com.carbounty.event.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.carbounty.event.NewTokenRegistrationEvent;
import com.carbounty.helper.MailService;

@Component
public class NewTokenRegistrationEventListener implements ApplicationListener<NewTokenRegistrationEvent> {

	@Autowired
	private MailService mailService;

	@Override
	public void onApplicationEvent(NewTokenRegistrationEvent event) {
		String url = event.getApplicationUrl() + "/user/verifyUserRegistration?token=";
		mailService.sendHtmlMail(event.getUser().getEmail(), "Verify Your Account Here Using New-Token",
				"<!DOCTYPE html>\r\n" + "<html>\r\n" + "<head>\r\n" + "<meta charset=\"UTF-8\">\r\n"
						+ "<title>Email Verification</title>\r\n" + "</head>\r\n" + "<body>\r\n"
						+ "<h1>Email Verification</h1>\r\n" + "<p>Hello, " + event.getUser().getFirstName() + " "
						+ event.getUser().getLastName()
						+ "<p>Thank you for registering! Please click the link below to verify your email address:</p>\r\n"
						+ url + "<p>If you did not sign up for an account, please ignore this email.</p>\r\n"
						+ "<p>Best regards,<br>Car Bounty ;)</p>\r\n" + "</body>\r\n" + "</html>");
	}

}
