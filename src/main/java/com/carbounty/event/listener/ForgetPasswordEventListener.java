package com.carbounty.event.listener;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import com.carbounty.entity.User;
import com.carbounty.entity.UserForgetPasswordToken;
import com.carbounty.event.ForgetPasswordEvent;
import com.carbounty.helper.MailService;
import com.carbounty.repository.UserForgetPasswordTokenRepository;

public class ForgetPasswordEventListener implements ApplicationListener<ForgetPasswordEvent> {

	@Autowired
	private MailService mailSender;

	@Autowired
	private UserForgetPasswordTokenRepository userForgetPasswordTokenRepo;

	@Override
	public void onApplicationEvent(ForgetPasswordEvent event) {
		User user = event.getUser();
		String token = UUID.randomUUID().toString();
		UserForgetPasswordToken userForgetPasswordToken = new UserForgetPasswordToken(user, token);
		userForgetPasswordTokenRepo.save(userForgetPasswordToken);
		String url = event.getApplicationUrl() + "/user/verifyUserForgetPasswordToken?token=" + token;
		mailSender.sendHtmlMail(user.getEmail(), "Forget Password Request", "<!DOCTYPE html>\r\n" + "<html>\r\n"
				+ "<head>\r\n" + "    <meta charset=\"UTF-8\">\r\n" + "    <title>Password Reset Request</title>\r\n"
				+ "</head>\r\n" + "<body>\r\n" + "    <h1>Password Reset Request</h1>\r\n" + "    <p>Hello "
				+ user.getFirstName() + " " + user.getLastName() + ",</p>\r\n"
				+ "    <p>As per your request to reset your password, please click the link below to reset your password:</p>\r\n"
				+ url + "\r\n"
				+ "    <p>If you did not request a password reset or have any concerns, please contact our support team immediately.</p>\r\n"
				+ "    <p>Best regards,<br>Car Bounty ;)</p>\r\n" + "</body>\r\n" + "</html>\r\n" + "");
	}

}
