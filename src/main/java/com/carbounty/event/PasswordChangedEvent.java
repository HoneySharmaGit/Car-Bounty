package com.carbounty.event;

import org.springframework.context.ApplicationEvent;

import com.carbounty.entity.User;

@SuppressWarnings("serial")
public class PasswordChangedEvent extends ApplicationEvent {

	private final User user;

	public PasswordChangedEvent(User user) {
		super(user);
		this.user = user;
	}

	public User getUser() {
		return user;
	}

}
