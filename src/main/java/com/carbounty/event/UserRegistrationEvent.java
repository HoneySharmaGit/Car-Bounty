package com.carbounty.event;

import org.springframework.context.ApplicationEvent;

import com.carbounty.entity.User;

@SuppressWarnings("serial")
public class UserRegistrationEvent extends ApplicationEvent {

	public UserRegistrationEvent(User user, String applicationUrl) {
		super(user);
		this.user = user;
		this.applicationUrl = applicationUrl;
	}

	private final User user;

	private final String applicationUrl;

	public User getUser() {
		return user;
	}

	public String getApplicationUrl() {
		return applicationUrl;
	}

}
