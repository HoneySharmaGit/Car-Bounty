package com.carbounty.event;

import org.springframework.context.ApplicationEvent;

import com.carbounty.entity.User;

@SuppressWarnings("serial")
public class ForgetPasswordEvent extends ApplicationEvent {

	private User user;

	private final String applicationUrl;

	public ForgetPasswordEvent(User user, String applicationUrl) {
		super(user);
		this.user = user;
		this.applicationUrl = applicationUrl;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getApplicationUrl() {
		return applicationUrl;
	}

}
