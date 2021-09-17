package com.benz.api.exceptions;

public class InvalidRequestException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private String message;

	public InvalidRequestException(String message) {
		super(message);
		this.message = message;
	}

}
