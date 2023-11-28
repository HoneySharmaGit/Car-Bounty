package com.carbounty.exception;

@SuppressWarnings("serial")
public class PasswordNotMatchException extends Exception {

	public PasswordNotMatchException(String message) {
		super(message);
	}
}
