package com.carbounty.event.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.carbounty.event.PasswordChangedEvent;
import com.carbounty.helper.MailService;

@Component
@Async
public class PasswordChangedEventListener implements ApplicationListener<PasswordChangedEvent> {

	@Autowired
	private MailService mailService;

	@Override
	public void onApplicationEvent(PasswordChangedEvent event) {
		mailService.sendHtmlMail(event.getUser().getEmail(), "Password Changed Successfully", "<!DOCTYPE html>\r\n"
				+ "<html>\r\n" + "<head>\r\n" + "    <meta charset=\"UTF-8\">\r\n"
				+ "    <title>Password Reset Request</title>\r\n" + "</head>\r\n" + "<body>\r\n"
				+ "    <h1>Password Reset Request</h1>\r\n" + "    <p>Hello " + event.getUser().getFirstName()
				+ ",</p>\r\n"
				+ "    <p>As per you request to reset your password. We have successfully changed your password.</p>\r\n"
				+ "    <p>If you did not request a password reset or have any concerns, please contact our support team immediately.</p>\r\n"
				+ "    <p>Best regards,<br>Car Bounty ;)</p>\r\n" + "</body>\r\n" + "</html>\r\n");
	}

}
