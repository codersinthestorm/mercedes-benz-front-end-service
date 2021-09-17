package com.benz.api.exceptions;

public class MessageBusException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private String message;

	public MessageBusException(String message) {
		super(message);
		this.message = message;
	}
}
