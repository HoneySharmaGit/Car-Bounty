package com.carbounty.event.listener;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.carbounty.entity.User;
import com.carbounty.entity.UserRegistrationToken;
import com.carbounty.event.UserRegistrationEvent;
import com.carbounty.helper.MailService;
import com.carbounty.repository.UserRegistrationTokenRepository;

@Component
public class UserRegistrationEventListener implements ApplicationListener<UserRegistrationEvent> {

	@Autowired
	private MailService mailService;

	@Autowired
	private UserRegistrationTokenRepository userRegisrationTokenRepo;

	@Override
	@Async
	public void onApplicationEvent(UserRegistrationEvent event) {
		User user = event.getUser();
		String token = UUID.randomUUID().toString();
		UserRegistrationToken userToken = new UserRegistrationToken(user, token);
		userRegisrationTokenRepo.save(userToken);
		String url = event.getApplicationUrl() + "/user/verifyUserRegistration?token=" + token;
		// send email to user.
		mailService.sendHtmlMail(user.getEmail(), "Verify Your Account Here", "<!DOCTYPE html>\r\n" + "<html>\r\n"
				+ "<head>\r\n" + "    <meta charset=\"UTF-8\">\r\n" + "    <title>Email Verification</title>\r\n"
				+ "</head>\r\n" + "<body>\r\n" + "    <h1>Email Verification</h1>\r\n" + "    <p>Hello "
				+ user.getFirstName() + " " + user.getLastName() + ",</p>\r\n"
				+ "    <p>Thank you for registering! Please click the link below to verify your email address:</p>\r\n"
				+ url + "    <p>If you did not sign up for an account, please ignore this email.</p>\r\n"
				+ "    <p>Best regards,<br>Car Bounty ;)</p>\r\n" + "</body>\r\n" + "</html>\r\n");
	}

}
